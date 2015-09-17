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

__author__ = 'galha'

import builtins
from concurrent.futures.thread import ThreadPoolExecutor
import time
from unittest.mock import Mock, patch, mock_open
import unittest
from appinsights.dockerinjector import DockerInjector
import re


class TestDockerInjector(unittest.TestCase):
    def test_inject(self):
        expected = 'file already exists'
        container = {'Id': 'c1'}
        wrapper_mock = Mock()
        wrapper_mock.get_containers.return_value = [container]
        wrapper_mock.run_command.return_value = 'file already exists'
        injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
        results = injector.inject_context()
        for c, result in results:
            self.assertEqual(container['Id'], c)
            self.assertEqual(expected, result)

    def test_get_my_container_id_when_file_exists(self):
        template = 'Docker container name=/boring_brattain,Docker image=ttt,Docker container id={0},Docker host=galha-ubuntu'
        expected_id = 'cd9d134b64807148faa24a17519c8e1a2650b825d4d38944ac54281b2dd1d94e'
        data = template.format(expected_id)
        with patch('os.path.exists') as exists:
            exists.return_value = True
            with patch.object(builtins, 'open', mock_open(read_data=data)):
                wrapper_mock = Mock()
                wrapper_mock.get_containers.return_value = [{'Id': 'c1'}]
                wrapper_mock.run_command.return_value = 'file already exists'
                injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
                id = injector.get_my_container_id()
                self.assertEqual(0, wrapper_mock.run_command.call_count)
                self.assertEqual(expected_id, id)

    def test_get_my_container_id_when_file_exists_with_new_line(self):
        template = 'Docker container name=/boring_brattain,Docker image=ttt,Docker host=galha-ubuntu,Docker container id={0}\n'
        expected_id = 'cd9d134b64807148faa24a17519c8e1a2650b825d4d38944ac54281b2dd1d94e'
        data = template.format(expected_id)
        with patch('os.path.exists') as exists:
            exists.return_value = True
            with patch.object(builtins, 'open', mock_open(read_data=data)):
                wrapper_mock = Mock()
                wrapper_mock.get_containers.return_value = [{'Id': 'c1'}]
                wrapper_mock.run_command.return_value = 'file already exists'
                injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
                id = injector.get_my_container_id()
                self.assertEqual(0, wrapper_mock.run_command.call_count)
                self.assertEqual(expected_id, id)

    def test_get_my_container_id_when_file_not_exists(self):
        template = 'Docker container name=/boring_brattain,Docker image=ttt,Docker container id={0},Docker host=galha-ubuntu'
        expected_id = 'cd9d134b64807148faa24a17519c8e1a2650b825d4d38944ac54281b2dd1d94e'
        data = template.format(expected_id)
        with patch('os.path.exists') as exists:
            exists.side_effect = [False, True]
            with patch.object(builtins, 'open', mock_open(read_data=data)):
                wrapper_mock = Mock()
                wrapper_mock.get_containers.return_value = [{'Id': 'c1'}]
                wrapper_mock.run_command.return_value = 'file already exists'
                injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
                id = injector.get_my_container_id()
                self.assertEqual(2, wrapper_mock.run_command.call_count)
                self.assertEqual(expected_id, id)

    def test_start(self):
        expected_c1 = {'Docker container id': 'c1', 'Docker host': 'host', 'Docker container name': 'name1', 'Docker image': 'image1'}
        expected_c2 = {'Docker container id': 'c2', 'Docker host': 'host', 'Docker container name': 'name2', 'Docker image': 'image2'}
        container = {'Id': 'c1', 'Image':'image1', 'Names':['name1']}

        wrapper_mock = Mock()
        wrapper_mock.get_containers.return_value = [container]
        wrapper_mock.get_host_name.return_value='host'
        wrapper_mock.run_command.return_value = 'file already exists'
        wrapper_mock.get_events.return_value = [{'time': 1439388853, 'Id': 'c2', 'id': 'c2', 'from': 'image2', 'status': 'start'}]
        wrapper_mock.get_inspection.return_value = {'Id': 'c2', 'status': 'start', 'Config':{'Image':'image2'}, 'Name':'name2'}

        def assert_func():
            res = wrapper_mock.run_command.mock_calls
            start = time.time()
            while len(res) < 4 and time.time() - start < 100:
                time.sleep(1)
            self.assertEqual(4, len(res))

        injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
        with ThreadPoolExecutor(max_workers=3) as ex:
            ex.submit(lambda: injector.start())
            result = ex.submit(lambda: assert_func())
            result.result()
            calls_argument_keys = [keys for name, args, keys in wrapper_mock.run_command.mock_calls]
            c1_calls = [d for d in calls_argument_keys if 'container' in d and 'Id' in d['container'] and d['container']['Id'] == 'c1']
            c2_calls = [d for d in calls_argument_keys if 'container' in d and 'Id' in d['container'] and d['container']['Id'] == 'c2']
            self.assertEqual(2, len(c1_calls))
            self.assertEqual(2, len(c2_calls))
            c1_data = c1_calls[1]['cmd']
            m1 = re.search('printf \'(.+)\'', c1_data)
            self.assertTrue(m1)
            actual_c1= {key:val for key,val in [token.split('=') for token in m1.group(1).strip(' ').split(',')]}
            self.assertDictEqual(expected_c1, actual_c1)
            c2_data = c2_calls[1]['cmd']
            m2 = re.search('printf \'(.+)\'', c2_data)
            self.assertTrue(m2)
            actual_c2= {key:val for key,val in [token.split('=') for token in m2.group(1).strip(' ').split(',')]}
            self.assertDictEqual(expected_c2, actual_c2)