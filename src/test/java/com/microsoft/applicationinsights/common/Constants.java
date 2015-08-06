package com.microsoft.applicationinsights.common;

/**
 * Created by yonisha on 7/26/2015.
 */
public class Constants {
    public static final String DEFAULT_STATE_EVENT = "{'name':'docker-container-state','properties':{'docker-image':'ubuntu','Created':'2015-08-06T09:01:55.3422261Z','StartedAt':'2015-08-06T09:01:55.7843711Z','docker-container-name':'/modest_jang0','RestartCount':0,'status':'start','docker-container-id':'22e26598fcda9d72c6c7aa55c9ee5c91b52ff73c31202f163f1bcbf2668c2f18','docker-host':'galha-ubuntu'}}";
    public static final String DEFAULT_METRIC_EVENT = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'docker-image':'x','docker-host':'x','docker-container-id':'x','docker-container-name':'x'}}";
    public static final String DEFAULT_METRIC_TEMPLATE = "{'metric':{'name':'%s','value':%s,'count':%s,'min':%s,'max':%s,'std':%s},'properties':{'docker-image':'%s','docker-host':'%s','docker-container-id':'%s','docker-container-name':'%s'}}";
}
