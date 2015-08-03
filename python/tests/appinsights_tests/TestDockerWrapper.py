__author__ = 'galha'
import unittest
from appinsights.dockerwrapper import DockerClientWrapper
from unittest.mock import Mock
from docker import Client
from docker.errors import APIError

class TestDockerClientWrapper(unittest.TestCase):
    def test_constructor_raise_when_docker_client_is_none(self):
        self.assertRaises(AssertionError, DockerClientWrapper, None)

    def test_get_host_name_gets_the_client_host_name(self):
        expecte_host_name = 'host'
        mock = Mock(spec=Client)
        mock.info.return_value={'Name':expecte_host_name}
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_host_name()
        self.assertEqual(expecte_host_name, actual)

    def test_get_host_name_returns_na_when_no_host_name_found(self):
        expecte_host_name = 'N/A'
        mock = Mock(spec=Client)
        mock.info.return_value={}
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_host_name()
        self.assertEqual(expecte_host_name, actual)

    def test_get_containers_gets_the_client_containers(self):
        expected_containers = ['c1','c2','c3']
        mock = Mock(spec=Client)
        mock.containers.return_value=expected_containers
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_containers()
        self.assertEqual(expected_containers, actual)

    def test_get_stats_gets_the_client_stats(self):
        expected_stats = ['s1','s2','s3']
        mock = Mock(spec=Client)
        mock.stats.return_value=expected_stats
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1',3)
        self.assertEqual(expected_stats, [stat for time, stat in actual])

    def test_get_stats_gets_requested_number_of_stats_from_the_client(self):
        mock = Mock(spec=Client)
        mock.stats.return_value=map(lambda i: "s{0}".format(i), range(0,100000))
        expected = ["s0","s1","s2"]
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1',3)
        self.assertEqual(expected, [stat for time, stat in actual])

    def test_get_stats_return_empty_list_on_api_error(self):
        mock = Mock()
        mock.stats.side_effect=APIError("boom", "boom", "boom")
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1',3)
        self.assertEqual([], [stat for time, stat in actual])