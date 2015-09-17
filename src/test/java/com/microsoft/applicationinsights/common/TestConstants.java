/*
 * ApplicationInsights-Docker
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.common;

/**
 * Created by yonisha on 7/26/2015.
 */
public class TestConstants {
    public static final String DEFAULT_STATE_EVENT = "{'name':'docker-container-state','ikey':'instrumentation_key','properties':{'Docker image':'ubuntu','Created':'2015-08-06T09:01:55.3422261Z','StartedAt':'2015-08-06T09:01:55.7843711Z','Docker container name':'/modest_jang0','RestartCount':0,'status':'start','Docker container id':'con_id','Docker host':'galha-ubuntu'}}";
    public static final String DEFAULT_METRIC_EVENT = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'Docker image':'x','Docker host':'x','Docker container id':'x','Docker container name':'x'}}";
    public static final String DEFAULT_METRIC_TEMPLATE = "{'metric':{'name':'%s','value':%s,'count':%s,'min':%s,'max':%s,'std':%s},'properties':{'Docker image':'%s','Docker host':'%s','Docker container id':'%s','Docker container name':'%s'}}";
}
