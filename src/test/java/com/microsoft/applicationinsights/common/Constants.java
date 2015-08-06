package com.microsoft.applicationinsights.common;

/**
 * Created by yonisha on 7/26/2015.
 */
public class Constants {
    public static final String DEFAULT_STATE_EVENT = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'docker-image':'x','docker-host':'x','docker-container-id':'x','docker-container-name':'x'}}";
    public static final String DEFAULT_METRIC_EVENT = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'docker-image':'x','docker-host':'x','docker-container-id':'x','docker-container-name':'x'}}";
    public static final String DEFAULT_METRIC_TEMPLATE = "{'metric':{'name':'%s','value':%s,'count':%s,'min':%s,'max':%s,'std':%s},'properties':{'docker-image':'%s','docker-host':'%s','docker-container-id':'%s','docker-container-name':'%s'}}";
}
