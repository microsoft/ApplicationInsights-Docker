__author__ = 'galha'

import unittest
from unittest.mock import patch
from unittest.mock import Mock
from appinsights.dockerwrapper import DockerClientWrapper
from appinsights.dockercollector import DockerCollector

class TestDockerCollector(unittest.TestCase):
    def test_collect_and_send(self):
        events = []
        properties = {'p1':'v1','p2':'v2'}
        metrics = ['m1','m2','m3']
        containers = ['c1','c2','c3']
        stats = ['s1','s2','s3']
        host_name = 'host'
        with patch('appinsights.dockerconvertors.get_container_properties') as properties_mock:
            with patch('appinsights.dockerconvertors.convert_to_metrics') as to_metric_mock:
                properties_mock.return_value = properties
                to_metric_mock.return_value = metrics
                mock = Mock(spec=DockerClientWrapper)
                mock.get_host_name.return_value = host_name
                mock.get_containers.return_value = containers
                mock.get_stats.return_value = stats
                collector = DockerCollector(mock, 3, lambda x: events.append(x))
                collector.collect_and_send()
                expected_metrics = [{'metric':metric, 'properties': properties} for container in containers for metric in metrics]
                for sent_event in events:
                    self.assertIn(sent_event ,expected_metrics)

    def test_collect_and_send_dont_send_events_when_no_containers(self):
        events = []
        properties = {'p1':'v1','p2':'v2'}
        metrics = ['m1','m2','m3']
        containers = []
        stats = ['s1','s2','s3']
        host_name = 'host'
        with patch('appinsights.dockerconvertors.get_container_properties') as properties_mock:
            with patch('appinsights.dockerconvertors.convert_to_metrics') as to_metric_mock:
                properties_mock.return_value = properties
                to_metric_mock.return_value = metrics
                mock = Mock(spec=DockerClientWrapper)
                mock.get_host_name.return_value = host_name
                mock.get_containers.return_value = containers
                mock.get_stats.return_value = stats
                collector = DockerCollector(mock, 3, lambda x: events.append(x))
                collector.collect_and_send()
                self.assertEqual(0, len(events))

    def test_collect_and_send_dont_send_events_when_no_metrics(self):
        events = []
        properties = {'p1':'v1','p2':'v2'}
        metrics = []
        containers = ['c1']
        stats = []
        host_name = 'host'
        with patch('appinsights.dockerconvertors.get_container_properties') as properties_mock:
            with patch('appinsights.dockerconvertors.convert_to_metrics') as to_metric_mock:
                properties_mock.return_value = properties
                to_metric_mock.return_value = metrics
                mock = Mock(spec=DockerClientWrapper)
                mock.get_host_name.return_value = host_name
                mock.get_containers.return_value = containers
                mock.get_stats.return_value = stats
                collector = DockerCollector(mock, 3, lambda x: events.append(x))
                collector.collect_and_send()
                self.assertEqual(0, len(events))

