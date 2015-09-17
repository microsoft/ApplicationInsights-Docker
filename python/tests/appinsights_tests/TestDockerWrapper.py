#
# ApplicationInsights-Docker
# Copyright (c) Microsoft Corporation
# All rights reserved.
#
# MIT License
# Permission is hereby granted, free of charge, to any person obtaining a copy of this
# software and associated documentation files (the ""Software""), to deal in the Software
# without restriction, including without limitation the rights to use, copy, modify, merge,
# publish, distribute, sublicense, and/or sell copies of the Software, and to permit
# persons to whom the Software is furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in all copies or
# substantial portions of the Software.
# THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
# FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.
#

from requests.packages.urllib3.exceptions import ReadTimeoutError

__author__ = 'galha'
import unittest
from appinsights.dockerwrapper import DockerClientWrapper, DockerWrapperError, get_production_docker_wrapper, \
    ProductionWrapper
from unittest.mock import Mock, patch, call
from docker import Client
from docker.errors import APIError


class TestDockerClientWrapper(unittest.TestCase):
    def test_constructor_raise_when_docker_client_is_none(self):
        self.assertRaises(AssertionError, DockerClientWrapper, None)

    def test_get_host_name_gets_the_client_host_name(self):
        expecte_host_name = 'host'
        mock = Mock(spec=Client)
        mock.info.return_value = {'Name': expecte_host_name}
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_host_name()
        self.assertEqual(expecte_host_name, actual)

    def test_get_host_name_returns_na_when_no_host_name_found(self):
        expecte_host_name = 'N/A'
        mock = Mock(spec=Client)
        mock.info.return_value = {}
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_host_name()
        self.assertEqual(expecte_host_name, actual)

    def test_get_containers_gets_the_client_containers(self):
        expected_containers = ['c1', 'c2', 'c3']
        mock = Mock(spec=Client)
        mock.containers.return_value = expected_containers
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_containers()
        self.assertEqual(expected_containers, actual)

    def test_get_stats_gets_the_client_stats(self):
        expected_stats = ['s1', 's2', 's3']
        mock = Mock(spec=Client)
        mock.stats.return_value = expected_stats
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1', 3)
        self.assertEqual(expected_stats, [stat for time, stat in actual])

    def test_get_stats_gets_requested_number_of_stats_from_the_client(self):
        mock = Mock(spec=Client)
        mock.stats.return_value = map(lambda i: "s{0}".format(i), range(0, 100000))
        expected = ["s0", "s1", "s2"]
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1', 3)
        self.assertEqual(expected, [stat for time, stat in actual])

    def test_get_stats_return_empty_list_on_api_error(self):
        mock = Mock()
        mock.stats.side_effect = APIError("boom", "boom", "boom")
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1', 3)
        self.assertEqual([], [stat for time, stat in actual])

    def test_get_stats_return_empty_list_on_ReadTimeoutError(self):
        mock = Mock()
        mock.stats.side_effect = ReadTimeoutError('pool', 'url', 'message')
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1', 3)
        self.assertEqual([], [stat for time, stat in actual])

    def test_get_stats_return_empty_list_on_timeout(self):
        mock = Mock()
        mock.stats.side_effect = ReadTimeoutError('pool', 'url', 'message')
        wrapper = DockerClientWrapper(mock)
        actual = wrapper.get_stats('c1', 3)
        self.assertEqual([], [stat for time, stat in actual])

    def test_run_command(self):
        expectedResult = b'result'
        mock = Mock()
        mock.exec_create.return_value = 'exec1'
        mock.exec_start.return_value = expectedResult
        wrapper = DockerClientWrapper(mock)
        result = wrapper.run_command('c1', 'ls')
        self.assertEqual(expectedResult.decode('utf-8'), result)

    def test_run_command_raise_docker_wrapper_error(self):
        expectedResult = b'result'
        mock = Mock()
        mock.exec_create.side_effect = ReadTimeoutError('pool', 'url', 'message')
        mock.exec_start.return_value = expectedResult
        wrapper = DockerClientWrapper(mock)
        self.assertRaises(DockerWrapperError, wrapper.run_command, 'c1', 'ls')

    def test_production_wrapper(self):
        with patch('appinsights.dockerwrapper.ProductionWrapper') as mock:
            get_production_docker_wrapper("unix://docker.sock")
            self.assertEqual(1, mock.call_count)
            mock.assert_has_calls([call(base_url="unix://docker.sock")])

    def test_production_wrapper_uses_two_clients(self):
        with patch('appinsights.dockerwrapper.DockerClientWrapper') as mock:
            m1, m2 = Mock(), Mock()
            mock.side_effect = [m1, m2]
            wrapper = ProductionWrapper("unix://docker.sock")
            self.assertEqual(2, mock.call_count)
            wrapper.get_containers()
            wrapper.run_command(None, None)
            m1c = m1.get_containers.call_count
            m1r = m1.run_command.call_count
            m2c = m2.get_containers.call_count
            m2r = m2.run_command.call_count
            self.assertEqual(1, m1c + m1r)
            self.assertEqual(1, m2c + m2r)
            self.assertEqual(1, m1c + m2c)
            self.assertEqual(1, m1r + m2r)

    def test_get_inspect(self):
        expectedResult = 'result'
        mock = Mock()
        mock.inspect_container.return_value = expectedResult
        wrapper = DockerClientWrapper(mock)
        inspection = wrapper.get_inspection({'Id':'c1'})
        self.assertEqual(expectedResult, inspection)

