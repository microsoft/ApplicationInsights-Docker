__author__ = 'galha'

import builtins
from concurrent.futures.thread import ThreadPoolExecutor
import time
from unittest.mock import Mock, patch, mock_open
import unittest
from appinsights.dockerinjector import DockerInjector


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
        template = 'docker-container-name=/boring_brattain,docker-image=ttt,docker-container-id={0},docker-host=galha-ubuntu'
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
        template = 'docker-container-name=/boring_brattain,docker-image=ttt,docker-container-id={0},docker-host=galha-ubuntu'
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
        container = {'Id': 'c1'}

        wrapper_mock = Mock()
        wrapper_mock.get_containers.return_value = [container]
        wrapper_mock.run_command.return_value = 'file already exists'
        wrapper_mock.get_events.return_value = [{'Id': 'c2', 'status': 'start'}]

        def assert_func():
            res = wrapper_mock.run_command.mock_calls
            start = time.time()
            while len(res) < 4 and time.time() - start < 5:
                time.sleep(1)
            self.assertEqual(4, len(res))

        injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
        with ThreadPoolExecutor(max_workers=3) as ex:
            ex.submit(lambda: injector.start())
            result = ex.submit(lambda: assert_func())
            result.result()
            calls_argument_keys = [keys for name, args, keys in wrapper_mock.run_command.mock_calls]
            self.assertEqual(2, len([d for d in calls_argument_keys if 'container' in d and 'Id' in d['container'] and d['container']['Id'] == 'c1']))
            self.assertEqual(2, len([d for d in calls_argument_keys if 'container' in d and 'Id' in d['container'] and d['container']['Id'] == 'c2']))

    def test_when_inject_fail_start_fail(self):
        container = {'Id': 'c1'}
        wrapper_mock = Mock()
        wrapper_mock.get_containers.return_value = [container]
        wrapper_mock.run_command.side_effect= ['file already exists', Exception('Boom')]
        wrapper_mock.get_events.return_value = [{'Id': 'c2', 'status': 'start'}]
        injector = DockerInjector(docker_wrapper=wrapper_mock, docker_info_path="/path/docker.info")
        injector.start()
