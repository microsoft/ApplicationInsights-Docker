__author__ = 'galha'

import unittest
import itertools
import statistics
import time
from appinsights.dockercollector import DockerCollector

samples = [(i, 1234567, i, i + 300, 20 * i, 13 * i, 51*i) for i in range(0, 1000)]

stats = [{'cpu_stats': {'cpu_usage': {'total_usage': cpu}, 'system_cpu_usage': system},
          'memory_stats': {'limit': limit, 'usage': mem}, 'network': {'rx_bytes': rx, 'tx_bytes': tx}
          ,'blkio_stats': {'io_service_bytes_recursive':[{'op':'Total', 'value':blkio}]}} for
         mem, limit, cpu, system, rx, tx, blkio in samples]


class TestDockerCollector(unittest.TestCase):
    def test_constructor_trows_when_docker_client_is_none(self):
        self.assertRaises(AssertionError, DockerCollector, None)

    def test_docker_collector_throws_when_sample_count_less_than_2(self):
        self.assertRaises(AssertionError, DockerCollector, TestDockerWrapper(None), samples_in_each_metric=1, send_event=print)

    def test_collect_and_send(self):

        events_expected = 5
        world = DockerWorld("host1", [Container(image="image1", container_id="1", name="name1", stats=stats)])
        test_docker_wrapper = TestDockerWrapper(world)
        events = []
        collector = DockerCollector(docker_wrapper=test_docker_wrapper, send_event=lambda x: events.append(x),
                                    samples_in_each_metric=events_expected)
        collector.collect_and_send()
        metrics = itertools.groupby(events, lambda item: item["metric"]["name"])
        self.assertEquals(5, len(events))

        metrics = {key:{v['properties']['docker-container-id']:v for v in group} for key, group in metrics}
        self.assertTrue("docker-cpu-usage" in metrics)
        self.assertEqual(events_expected-1, metrics["docker-cpu-usage"]["1"]["metric"]["count"])
        self.assertTrue("docker-available-memory-mb" in metrics)
        self.assertEqual(events_expected, metrics["docker-available-memory-mb"]["1"]["metric"]["count"])
        self.assertTrue("docker-rx-bytes" in metrics)
        self.assertEqual(events_expected-1, metrics["docker-rx-bytes"]["1"]["metric"]["count"])
        self.assertTrue("docker-tx-bytes" in metrics)
        self.assertEqual(events_expected-1, metrics["docker-tx-bytes"]["1"]["metric"]["count"])

class DockerWorld(object):
    def __init__(self, host_name, containers):
        super().__init__()
        self.host_name = host_name
        self.containers = {c.id: c for c in containers}


class Container(object):
    def __init__(self, image, container_id, name, stats):
        super().__init__()
        self._stats = stats
        self.name = name
        self.image = image
        self.id = container_id

    def stats(self):
        return self._stats

    def as_dictionary(self):
        return {'Image': self.image, 'Id': self.id, 'Names': [self.name]}


class TestDockerWrapper(object):
    def __init__(self, docker_world):
        super().__init__()
        self.docker_world = docker_world

    def get_name(self):
        return {'Name': self.docker_world.host_name}

    def get_containers(self):
        return [v.as_dictionary() for v in self.docker_world.containers.values()]

    def get_stats(self, container, stats_to_bring):
        t0 = time.time()-100
        time_series = [t0+i for i in range(0,100)]
        key = container['Id']
        c = self.docker_world.containers[key]
        stats = c.stats();
        return [s for s in zip(time_series, stats[:min(stats_to_bring, len(stats))])]
