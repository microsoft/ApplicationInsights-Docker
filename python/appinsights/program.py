import sys

__author__ = 'galha'
import time

from docker import Client

from appinsights.dockercollector import DockerCollector

# args = parser.parse_args(['9c139518-d405-4c1b-a6c0-f3cddbdc25e8', '-d', 'http://10.165.225.7:4243'])
# args = parser.parse_args()
# print('ikey: {0}'.format(args.ikey))
# print(args)

def run(docker_socket):
    docker_client = Client(base_url=docker_socket)
    collector = DockerCollector(
        docker_client=docker_client,
        send_event=print,
        samples_in_each_metric=5)

    while True:
        collector.collect_and_send()
        time.sleep(10)
