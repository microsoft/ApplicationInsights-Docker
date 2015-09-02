package com.microsoft.applicationinsights.common;

/**
 * Created by yonisha on 7/26/2015.
 */
public class TestConstants {
    public static final String DEFAULT_STATE_EVENT = "{'name':'docker-container-state','ikey':'instrumentation_key','properties':{'Docker image':'ubuntu','Created':'2015-08-06T09:01:55.3422261Z','StartedAt':'2015-08-06T09:01:55.7843711Z','Docker container name':'/modest_jang0','RestartCount':0,'status':'start','Docker container id':'con_id','Docker host':'galha-ubuntu'}}";
    public static final String DEFAULT_METRIC_EVENT = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'Docker image':'x','Docker host':'x','Docker container id':'x','Docker container name':'x'}}";
    public static final String DEFAULT_METRIC_TEMPLATE = "{'metric':{'name':'%s','value':%s,'count':%s,'min':%s,'max':%s,'std':%s},'properties':{'Docker image':'%s','Docker host':'%s','Docker container id':'%s','Docker container name':'%s'}}";
}
