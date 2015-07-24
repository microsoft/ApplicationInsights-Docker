import docker

__author__ = 'galha'

import concurrent.futures
import itertools
import json
import statistics
from collections import namedtuple


class DockerCollector(object):
    HostStats = namedtuple('HostStats', ['host_name', 'container_stats'])
    ContainerStats = namedtuple('ContainerStats', ['container', 'stats_samples'])
    Metric = namedtuple('Metric', ['name', 'value', 'count', 'min', 'max', 'std'])
    AiMetrics = namedtuple('AiMetrics', ['properties', 'metric_list'])

    def __init__(self, docker_client, samples_in_each_metric=2, send_event=print):
        super().__init__()
        self._docker_client = docker_client
        self._samples_in_each_metric = samples_in_each_metric
        self._send_event = send_event

    def collect_and_send(self):
        """
        Collects docker metrics from docker and sends them to appinsights
        cpu, memory, rx_bytes and tx_bytes
        """
        host_stats = self._collect_host_and_containers_stats()

        list_ai_metrics = [DockerCollector._to_ai_metrics(host_name=host_stats.host_name,
                                                             container=container_stats.container,
                                                             stats=container_stats.stats_samples) for
                           container_stats in
                           host_stats.container_stats]
        self._send_metrics(list_ai_metrics=list_ai_metrics)

    def _send_metrics(self, list_ai_metrics):
        for ai_metrics in list_ai_metrics:
            properties = ai_metrics.properties
            for metric in ai_metrics.metric_list:
                self._send_event(
                    {'metric': {'name': metric.name, 'value': metric.value, 'count': metric.count, 'min': metric.min,
                                'max': metric.max, 'std': metric.std}, 'properties': properties})

    def _get_all_containers_stats(self):
        """
        Gets the docker containers stats
        :return: List of ContainerStats(container, stats_samples)
        """
        containers = self._docker_client.containers()
        with concurrent.futures.ThreadPoolExecutor(max_workers=max(len(containers), 10)) as executor:
            return list(executor.map(lambda container: DockerCollector.ContainerStats(
                container=container,
                stats_samples=self._get_container_stats_samples(container=container)), containers))

    def _collect_host_and_containers_stats(self):
        """
        Collect the Ai Metrics of the docker
        :return: List of HostStats(host_name, container_stats)
        """
        info = self._docker_client.info()
        host_name = info.get('Name', 'N/A')
        container_stats = self._get_all_containers_stats()
        return DockerCollector.HostStats(host_name=host_name, container_stats=container_stats)

    @staticmethod
    def _aggregate_stats_to_metrics(stats):
        # Aggregates the container stats (stats json documents), to metrics
        # Returns the memory and cpu metrics
        # Each metric has the properties of value, count, min, max and std
        json_stats = list(map(lambda x: json.loads(x.decode()), stats))

        cpu_metric = DockerCollector._get_cpu_metric(json_stats=json_stats)
        memory_metric = DockerCollector._get_simple_metric(
            json_stats=json_stats,
            func=lambda stat: stat['memory_stats']['limit'] - stat['memory_stats']['usage'],
            metric_name='Available Bytes')

        rx_bytes_metric = DockerCollector._get_simple_metric(
            json_stats=json_stats,
            func=lambda stat: stat['network']['rx_bytes'],
            metric_name='docker-rx-bytes')

        tx_bytes_metric = DockerCollector._get_simple_metric(
            json_stats=json_stats,
            func=lambda stat: stat['network']['tx_bytes'],
            metric_name='docker-tx-bytes')

        return [memory_metric, cpu_metric, rx_bytes_metric, tx_bytes_metric]

    def _get_container_stats_samples(self, container):
        """
        Get the docker stats of a container
        :param container: The container
        :return: List of stats
        """
        try:
            return list(itertools.islice(self._docker_client.stats(container), 0, self._samples_in_each_metric, 1))
        except docker.errors.APIError as e:
            return []

    @staticmethod
    def _get_cpu_metric(json_stats):
        """
        gets the cpu metric form the stats list
        :param json_stats: list of json objects of stat
        :return: cpu metric
        """
        cpu_list = [stat['cpu_stats']['cpu_usage']['total_usage'] for stat in json_stats]
        system_cpu_list = [stat['cpu_stats']['system_cpu_usage'] for stat in json_stats]
        cpu2 = cpu_list[1:]
        cpu1 = cpu_list[:len(cpu_list) - 1]
        system2 = system_cpu_list[1:]
        system1 = system_cpu_list[: len(system_cpu_list) - 1]
        cpu_percents = [100.0 * (cpu_curr - cpu_prev) / (system_curr - system_prev) for
                        cpu_curr, cpu_prev, system_curr, system_prev in list(zip(cpu2, cpu1, system2, system1))]
        return DockerCollector.Metric(name='% Processor Time',
                                      value=statistics.mean(cpu_percents),
                                      count=len(cpu_percents),
                                      min=min(cpu_percents),
                                      max=max(cpu_percents),
                                      std=statistics.stdev(cpu_percents) if len(cpu_percents) > 1 else None)

    @staticmethod
    def _get_simple_metric(json_stats, func, metric_name):
        samples = [func(stat) for stat in json_stats]
        return DockerCollector.Metric(name=metric_name,
                                      value=statistics.mean(samples),
                                      count=len(samples),
                                      min=min(samples),
                                      max=max(samples),
                                      std=statistics.stdev(samples) if len(samples) > 1 else None)

    @staticmethod
    def _to_ai_metrics(host_name, container, stats):
        metrics = DockerCollector._aggregate_stats_to_metrics(stats)
        properties = {'docker-host': host_name,
                      'docker-image': container.get('Image', 'N/A'),
                      'docker-container-id': container.get('Id', 'N/A'),
                      'docker-container-name': container.get('Names', ['N/A'])[0]}

        return DockerCollector.AiMetrics(properties=properties, metric_list=metrics)
