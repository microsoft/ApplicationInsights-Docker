package com.microsoft.applicationinsights.python;

import java.io.IOException;

/**
 * Created by yonisha on 7/26/2015.
 */
public interface ProcessBuilder {
    Process start() throws IOException;
}
