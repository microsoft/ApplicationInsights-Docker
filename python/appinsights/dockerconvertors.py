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
import statistics

def convert_to_metrics(stats):
    """ convert the docker container stats list to ai metrices
    :param stats: The docker container stats list
    :return: List of Ai Metrics (cpu, rx, tx, blkio),
    each metric is a dict of (name, value, count, min, max, and std)
    """
    assert stats is not None and len(stats)>1 ,"stats should have at least 2 samples in it"
    return [get_cpu_metric(stats=stats),
            get_simple_metric(
                metric_name='Available Bytes',
                func=lambda stat: stat['memory_stats']['limit'] - stat['memory_stats']['usage'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='Docker RX Bytes',
                func=lambda stat: stat['network']['rx_bytes'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='Docker TX Bytes',
                func=lambda stat: stat['network']['tx_bytes'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='Docker Blkio Bytes',
                func= get_total_blkio,
                stats=stats
            )]

def get_total_blkio(stat):
    """ Gets the total blkio out of the docker stat
    :param stat: The docker stat
    :return: The blkio
    """
    io_list = stat['blkio_stats']['io_service_bytes_recursive']
    if len(io_list)>0:
        total_dics = list(filter(lambda dic: dic['op'] == 'Total', io_list))
        if len(total_dics)>0:
            return total_dics[0]['value']
    else:
        return 0

def get_cpu_metric(stats):
    """ Gets the cpu metric from the docker stats list
    :param stats: The docker stats list
    :return: A cpu metric
    """
    assert stats is not None and len(stats)>1 ,\
        "the 'stats' samples must contain more than 1 statistics in order to calclulate the cpu metric"

    cpu_list = [stat['cpu_stats']['cpu_usage']['total_usage'] for time, stat in stats]
    system_cpu_list = [stat['cpu_stats']['system_cpu_usage'] for time, stat in stats]
    cpu2 = cpu_list[1:]
    cpu1 = cpu_list[:len(cpu_list) - 1]
    system2 = system_cpu_list[1:]
    system1 = system_cpu_list[: len(system_cpu_list) - 1]
    cpu_percents = [100.0 * (cpu_curr - cpu_prev) / (system_curr - system_prev) for
                    cpu_curr, cpu_prev, system_curr, system_prev in list(zip(cpu2, cpu1, system2, system1))]

    return {'name':'% Processor Time',
            'value':statistics.mean(cpu_percents),
            'count':len(cpu_percents),
            'min':min(cpu_percents),
            'max':max(cpu_percents),
            'std':statistics.stdev(cpu_percents) if len(cpu_percents) > 1 else None}

def get_per_second_metric(metric_name, func, stats):
    """ Gets a per second metric out of the docker stats list,
    per second valuates the time difference in time between every two samples
    :param metric_name: The metric name
    :param func: A function which gets the value of the sample out of the stat object
    :param stats: The docker stats list
    :return: Ai metric
    """
    assert metric_name is not None, "metric_name shoud not be None"
    assert func is not None, "func should not be None"
    assert stats is not None and len(stats)>1, "stats should have more than 1 samples in it"
    stats2 = stats[1:]
    stats1 = stats[:len(stats) - 1]
    samples = [(func(s2)-func(s1)) / (time2 - time1) for (time2, s2), (time1, s1) in list(zip(stats2, stats1))]
    return {'name':metric_name,
            'value':statistics.mean(samples),
            'count':len(samples),
            'min':min(samples),
            'max':max(samples),
            'std':statistics.stdev(samples) if len(samples) > 1 else None}

def get_simple_metric(metric_name, func, stats):
    """ Gets an ai metric from the stats list (count, average, min, max and std)
    :param metric_name: The metric name
    :param func: A function which gets the value of the sample out of the stat object
    :param stats: The docker stats list
    :return: Ai metric
    """
    assert metric_name is not None
    assert func is not None
    assert stats is not None and len(stats)>1
    samples = [func(stat) for time, stat in stats]
    return {'name': metric_name,
            'value':statistics.mean(samples),
            'count':len(samples),
            'min':min(samples),
            'max':max(samples),
            'std':statistics.stdev(samples) if len(samples) > 1 else None}

def get_container_properties(container, host_name):
    """ Gets the container properties from a container object
    :param container: The container object
    :param host_name: The host name
    :return: dict of (Docker host, Docker image, Docker container id, Docker container name)
    """
    return {'Docker host': host_name,
            'Docker image': container.get('Image', 'N/A'),
            'Docker container id': container.get('Id', 'N/A'),
            'Docker container name': container.get('Names', ['N/A'])[0]}


def get_container_properties_from_inspect(inspect, host_name):
    """ Gets the container properties from an inspect object
    :param inspect: The inspect object
    :param host_name: The host name
    :return: dict of (Docker host, Docker image, Docker container id, Docker container name)
    """
    return {'Docker host': host_name,
            'Docker image': inspect['Config'].get('Image', 'N/A') if 'Config' in inspect else 'N/A',
            'Docker container id': inspect.get('Id', 'N/A'),
            'Docker container name': inspect.get('Names', [inspect.get('Name', 'N/A')])[0]}

