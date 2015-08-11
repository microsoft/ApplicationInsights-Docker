__author__ = 'galha'

from unittest.mock import Mock
import unittest
from appinsights.dockerinjector import DockerInjector

class TestDockerInjector(unittest.TestCase):
    def test_inject(self):
        expected = 'file already exists'
        container = {'Id':'c1'}
        wrapper_mock = Mock()
        wrapper_mock.get_containers.return_value=[container]
        wrapper_mock.run_command.return_value = 'file already exists'
        injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
        results = injector.inject()
        for c, result in results:
            self.assertEqual(container['Id'], c)
            self.assertEqual(expected, result)
