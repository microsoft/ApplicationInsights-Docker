import sys
from appinsights.dockerwrapper import DockerWrapperError

__author__ = 'galha'

import concurrent.futures, os, re
from appinsights import dockerconvertors


class DockerInjector(object):
    """
    Application insights docker container injector,
    used to inject the container context to running containers
    """
    _default_bash = "bash"
    _mkdir_template = "mkdir -p \"{directory}\""
    _create_file_template = "/bin/sh -c \"[ ! -f {directory}/{file} ] && `echo {properties} > {directory}/{file}` && echo created file || echo file already exists\""

    def __init__(self, docker_wrapper, docker_info_path):
        """ Initializes a new instance of the class.
        :param docker_wrapper: a docker wrapper instance
        :param docker_info_path: (str). The file path where to inject the context
        :return:
        """
        self._docker_wrapper = docker_wrapper
        self._docker_info_path = docker_info_path
        self._host_name = None
        self._dirName = os.path.dirname(docker_info_path)
        self._fileName = os.path.basename(docker_info_path)
        self._my_container_id = None

    def inject_context(self):
        """ Injects the context to all running containers
        :return:
        """
        containers = self._docker_wrapper.get_containers()
        if self._host_name is None:
            self._host_name = self._docker_wrapper.get_host_name()

        with concurrent.futures.ThreadPoolExecutor(max_workers=30) as executor:
            results = list(
                executor.map(lambda container: (container["Id"], self.inject_container(container)),containers))

        return results

    def start(self):
        """ start to inject the context to all running containers,
        and listen to containers events to inject to inject the context to new created containers.
        :return:
        """
        with concurrent.futures.ThreadPoolExecutor(max_workers=30) as executor:
            executor.submit(lambda : self.inject_context())
            executor.map(
                lambda event: self.inject_container(event),
                filter(
                    lambda event: event['status'] in ['start', 'restart', 'unpause'],
                    self._docker_wrapper.get_events()))

    @property
    def docker_info_path(self):
        """ Gets the docker info file path
        :return: The docker info file path
        """
        return self._docker_info_path

    def get_my_container_id(self):
        """ Gets the container id of the caller.
        :return: The container Id of the caller, None if the call was made not within a container
        """
        if self._my_container_id is not None:
            return self._my_container_id

        if not os.path.exists(self.docker_info_path):
            self.inject_context();

        # we are not running in a container
        if not os.path.exists(self.docker_info_path):
            return None

        # get the context from the injected file
        with open(self.docker_info_path, mode='r') as f:
            context = f.read()
            match = re.search('docker-container-id=([a-zA-Z0-9]+)', context)
            if match:
                self._my_container_id = match.group(1)
                return self._my_container_id

        # this happens only when we run the code not within a container
        return None

    def inject_container(self, container):
        """ Injects the container context into the given container
        :param container: The container to inject
        :return:
        """
        try:
            mkdir_cmd = DockerInjector._mkdir_template.format(directory=self._dirName)
            self._docker_wrapper.run_command(container=container, cmd=mkdir_cmd)
            properties = self._get_properties(container)
            properties_string = ",".join(["{key}={value}".format(key=k, value=v) for k, v in properties.items()])
            docker_info_cmd = DockerInjector._create_file_template.format(
                directory=self._dirName,
                file=self._fileName,
                properties=properties_string)

            result = self._docker_wrapper.run_command(container=container, cmd=docker_info_cmd)
            return result
        except DockerWrapperError as e:
            return e

    def _get_properties(self, item):
        if 'status' in item:
            inspect = self._docker_wrapper.get_inspection(item)
            return dockerconvertors.get_container_properties_from_inspect(inspect=inspect, host_name=self._host_name)
        return dockerconvertors.get_container_properties(container=item, host_name=self._host_name)