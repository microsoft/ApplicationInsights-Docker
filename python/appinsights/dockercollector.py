__author__ = 'galha'

import concurrent.futures
from appinsights import dockerconvertors
from collections import namedtuple


class DockerCollector(object):
    HostStats = namedtuple('HostStats', ['host_name', 'container_stats'])
    ContainerStats = namedtuple('ContainerStats', ['container', 'stats_samples'])
    Metric = namedtuple('Metric', ['name', 'value', 'count', 'min', 'max', 'std'])
    AiMetrics = namedtuple('AiMetrics', ['properties', 'metric_list'])

    def __init__(self, docker_wrapper, samples_in_each_metric=2, send_event=print):
        super().__init__()
        assert docker_wrapper is not None, 'docker_client cannot be None'
        assert samples_in_each_metric > 1, 'samples_in_each_metric must be greater than 1, given: {0}'.format(
            samples_in_each_metric)

        self._docker_wrapper = docker_wrapper
        self._samples_in_each_metric = samples_in_each_metric
        self._send_event = send_event

    def collect_and_send(self):
        """
        Collects docker metrics from docker and sends them to sender
        cpu, memory, rx_bytes ,tx_bytes, blkio metrics
        """

        host_name = self._docker_wrapper.get_host_name()
        containers = self._docker_wrapper.get_containers()
        with concurrent.futures.ThreadPoolExecutor(max_workers=max(len(containers), 30)) as executor:
            container_stats = list(
                executor.map(
                    lambda container: (container, self._docker_wrapper.get_stats(container=container,stats_to_bring=self._samples_in_each_metric)),
                    containers))

        for container, stats in container_stats:
            metrics = dockerconvertors.convert_to_metrics(stats)
            properties = dockerconvertors.get_container_properties(container, host_name)
            for metric in metrics:
                self._send_event({'metric': metric, 'properties': properties})