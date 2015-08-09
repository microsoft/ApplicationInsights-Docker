__author__ = 'galha'

import concurrent.futures
import time
from appinsights import dockerconvertors
import dateutil.parser
import sys

class DockerCollector(object):
    _cmd_template = "/bin/sh -c \"[ -f {file} ] && echo yes || echo no\""

    def _default_print(text):
        print(text, flush=True)

    def __init__(self, docker_wrapper, samples_in_each_metric=2, send_event=_default_print,
                 sdk_file='/usr/appinsights/docker/sdk.info'):
        super().__init__()
        assert docker_wrapper is not None, 'docker_client cannot be None'
        assert samples_in_each_metric > 1, 'samples_in_each_metric must be greater than 1, given: {0}'.format(
            samples_in_each_metric)
        self._sdk_file = sdk_file
        self._docker_wrapper = docker_wrapper
        self._samples_in_each_metric = samples_in_each_metric
        self._send_event = send_event
        self._containers_state = {}

    def collect_stats_and_send(self):
        """
        Collects docker metrics from docker and sends them to sender
        cpu, memory, rx_bytes ,tx_bytes, blkio metrics
        """

        host_name = self._docker_wrapper.get_host_name()
        containers = self._docker_wrapper.get_containers()
        self.update_containers_state(containers=containers)
        containers_without_sdk = [v['container'] for k, v in self._containers_state.items() if not v['sdk']]
        with concurrent.futures.ThreadPoolExecutor(max_workers=max(len(containers), 30)) as executor:
            container_stats = list(
                executor.map(
                    lambda container: (container, self._docker_wrapper.get_stats(container=container,
                                                                                 stats_to_bring=self._samples_in_each_metric)),
                    containers_without_sdk))

        for container, stats in container_stats:
            metrics = dockerconvertors.convert_to_metrics(stats)
            properties = dockerconvertors.get_container_properties(container, host_name)
            for metric in metrics:
                self._send_event({'metric': metric, 'properties': properties})

    def _container_has_sdk(self, container):
        result = self._docker_wrapper.run_command(container, DockerCollector._cmd_template.format(file=self._sdk_file))
        result = result.strip()
        return result == 'yes'

    def update_containers_state(self, containers):
        self._remove_old_containers(containers)
        with concurrent.futures.ThreadPoolExecutor(max_workers=max(len(containers), 30)) as executor:
            list(executor.map(lambda c: self._update_container_state(c), containers))

    def _remove_old_containers(self, containers):
        curr_containers = {c['Id']: c for c in containers}
        keys = [k for k in self._containers_state]
        for key in [key for key in keys if key not in curr_containers]:
            del self._containers_state[key]

    def _update_container_state(self, container):
        id = container['Id']
        if id not in self._containers_state:
            sdk = self._container_has_sdk(container)
            self._containers_state[id] = {'sdk': sdk, 'time': time.time(), 'container': container}
            return sdk
        status = self._containers_state[id]
        if status['sdk']:
            return True

        if status['time'] > time.time() - 60:
            sdk = self._container_has_sdk(container)
            status['sdk'] = sdk
            return sdk

        return False

    def collect_container_events(self):
        event_name_template = 'docker-container-{0}'
        host_name = self._docker_wrapper.get_host_name()
        for event in self._docker_wrapper.get_events():
            status = event['status']
            if status not in ['start', 'stop', 'die', 'restart', 'pause', 'unpause']:
                continue

            event_name = event_name_template.format(status)
            inspect = self._docker_wrapper.get_inspection(event)
            properties = dockerconvertors.get_container_properties_from_inspect(inspect, host_name)
            properties['status'] = status
            properties['Created'] = inspect['Created']
            properties['StartedAt'] = inspect['State']['StartedAt']
            properties['RestartCount'] = inspect['RestartCount']

            if status in ['stop', 'die']:
                properties['FinishedAt'] = inspect['State']['FinishedAt']
                properties['ExitCode'] = inspect['State']['ExitCode']
                properties['Error'] = inspect['State']['Error']
                duration = dateutil.parser.parse(properties['FinishedAt']) - dateutil.parser.parse(
                    properties['StartedAt'])
                duration_seconds = duration.total_seconds()
                properties['duration-seconds'] = duration_seconds
                properties['duration-minutes'] = duration_seconds / 60
                properties['duration-hours'] = duration_seconds / 3600
                properties['duration-days'] = duration_seconds / 86400
            event_data = {'name': event_name, 'properties': properties}
            self._send_event(event_data)
