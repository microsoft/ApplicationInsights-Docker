__author__ = 'galha'
import statistics

def convert_to_metrics(stats):
    assert stats is not None and len(stats)>1 ,"stats should have at least 2 samples in it"
    return [get_cpu_metric(stats=stats),
            get_simple_metric(
                metric_name='Available Bytes',
                func=lambda stat: stat['memory_stats']['limit'] - stat['memory_stats']['usage'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='docker-rx-bytes',
                func=lambda stat: stat['network']['rx_bytes'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='docker-tx-bytes',
                func=lambda stat: stat['network']['tx_bytes'],
                stats=stats
            ),
            get_per_second_metric(
                metric_name='docker-blkio-bytes',
                func= get_total_blkio,
                stats=stats
            )]

def get_total_blkio(stat):
    io_list = stat['blkio_stats']['io_service_bytes_recursive']
    if len(io_list)>0:
        total_dics = list(filter(lambda dic: dic['op'] == 'Total', io_list))
        if len(total_dics)>0:
            return total_dics[0]['value']
    else:
        return 0

def get_cpu_metric(stats):
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
    return {'docker-host': host_name,
            'docker-image': container.get('Image', 'N/A'),
            'docker-container-id': container.get('Id', 'N/A'),
            'docker-container-name': container.get('Names', ['N/A'])[0]}


def get_container_properties_from_inspect(container, host_name):
    return {'docker-host': host_name,
            'docker-image': container['Config'].get('Image', 'N/A'),
            'docker-container-id': container.get('Id', 'N/A'),
            'docker-container-name': container.get('Names', [container.get('Name', 'N/A')])[0]}

