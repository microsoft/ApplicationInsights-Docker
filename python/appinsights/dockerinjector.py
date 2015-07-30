__author__ = 'galha'

from appinsights import dockerconvertors
import concurrent.futures


class DockerInjector(object):
    _dir_name = "/usr/appinsights/docker"
    _default_bash = "bash"
    _file_name = "docker.info"
    _mkdir_template = "mkdir -p \"{directory}\""
    _create_file_template = "/bin/sh -c \"[ ! -f {directory}/{file} ] && `echo {properties} > {directory}/{file}` && echo created file || echo file already exists\""

    def __init__(self, docker_wrapper, docker_info_path):
        self._docker_wrapper = docker_wrapper
        self._docker_info_path = docker_info_path
        self._containers_injected = set()
        self._host_name = None

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
        mkdir_cmd = DockerInjector._mkdir_template.format(directory=DockerInjector._dir_name)
        self._docker_wrapper.run_command(container=container, cmd=mkdir_cmd)
        properties = dockerconvertors.get_container_properties(container=container, host_name=self._host_name)
        docker_info_cmd = DockerInjector._create_file_template.format(
            directory=DockerInjector._dir_name,
            file=DockerInjector._file_name,
            properties=properties)

        result = self._docker_wrapper.run_command(container=container, cmd=docker_info_cmd)
        self._containers_injected.add(container['Id'])
        return result
