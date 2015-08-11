from appinsights.dockerwrapper import DockerWrapperError

__author__ = 'galha'

import concurrent.futures
import os

from appinsights import dockerconvertors


class DockerInjector(object):
    _default_bash = "bash"
    _mkdir_template = "mkdir -p \"{directory}\""
    _create_file_template = "/bin/sh -c \"[ ! -f {directory}/{file} ] && `echo {properties} > {directory}/{file}` && echo created file || echo file already exists\""

    def __init__(self, docker_wrapper, docker_info_path):
        self._docker_wrapper = docker_wrapper
        self._docker_info_path = docker_info_path
        self._containers_injected = set()
        self._host_name = None
        self._dirName = os.path.dirname(docker_info_path)
        self._fileName = os.path.basename(docker_info_path)

    def inject(self):
        containers = self._docker_wrapper.get_containers()
        if self._host_name is None:
            self._host_name = self._docker_wrapper.get_host_name()

        with concurrent.futures.ThreadPoolExecutor(max_workers=30) as executor:
            results = list(
                executor.map(
                    lambda container: (container["Id"], self._inject_container(container)),
                    filter(
                        lambda container: container["Id"] not in self._containers_injected,
                        containers)))

        return results

    def _inject_container(self, container):
        try:
            mkdir_cmd = DockerInjector._mkdir_template.format(directory=self._dirName)
            self._docker_wrapper.run_command(container=container, cmd=mkdir_cmd)
            properties = dockerconvertors.get_container_properties(container=container, host_name=self._host_name)
            properties_string = ",".join(["{key}={value}".format(key=k, value=v) for k, v in properties.items()])
            docker_info_cmd = DockerInjector._create_file_template.format(
                directory=self._dirName,
                file=self._fileName,
                properties=properties_string)

            result = self._docker_wrapper.run_command(container=container, cmd=docker_info_cmd)
            self._containers_injected.add(container['Id'])
            return result
        except DockerWrapperError as e:
            return e
