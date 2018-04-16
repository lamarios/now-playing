package com.ftpix.nowplaying;

import java.util.List;

/**
 * Interface that will define endpoints for callbacks and get hte HTML for a login link
 */
public interface ExternalLoginPlugin extends Plugin{

    /**
     * Define endpoints that external parties can access.
     * The url will start by /external/{moduleid}/{your endpoint}
     * Follow sparkjava definition for parameters
     *
     * YOUR URL MUST START WITH /
     *
     * @return
     */
    List<ExternalEndPointDefinition> defineExternalEndPoints();

    String getLoginLinkHtml();
}
