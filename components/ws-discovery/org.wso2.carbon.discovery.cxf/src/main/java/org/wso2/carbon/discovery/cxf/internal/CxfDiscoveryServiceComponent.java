/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.discovery.cxf.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.discovery.cxf.CXFServiceInfo;
import org.wso2.carbon.discovery.cxf.CxfDiscoveryConfigurationContextObserver;
import org.wso2.carbon.discovery.cxf.DiscoveryStartupHandler;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @scr.component name="org.wso2.carbon.discovery.cxf" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class CxfDiscoveryServiceComponent {

    private static Log log = LogFactory.getLog(CxfDiscoveryServiceComponent.class);
    private Queue<CXFServiceInfo> initialMessagesList = new LinkedList<CXFServiceInfo>();

    private ServiceRegistration observerServiceRegistration;
    private CxfDiscoveryDataHolder dataHolder = CxfDiscoveryDataHolder.getInstance();

    protected void activate(ComponentContext ctx) {

        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(ServerStartupHandler.class.getName(), new DiscoveryStartupHandler(), null);

        // This will take care of registering observers in tenant axis configurations
        dataHolder.setInitialMessagesList(initialMessagesList);
        CxfDiscoveryConfigurationContextObserver configCtxObserver =
                new CxfDiscoveryConfigurationContextObserver(initialMessagesList);
        observerServiceRegistration = bundleContext.registerService(
                Axis2ConfigurationContextObserver.class.getName(),
                configCtxObserver, null);

        configCtxObserver.createdConfigurationContext(dataHolder.getMainServerConfigContext());
        if (log.isDebugEnabled()) {
            log.info("Activating CXF WS-Discovery Startup Publisher Component");
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating CXF WS-Discovery component");
        }

        if (observerServiceRegistration != null) {
            observerServiceRegistration.unregister();
            observerServiceRegistration = null;
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(null);
    }

}