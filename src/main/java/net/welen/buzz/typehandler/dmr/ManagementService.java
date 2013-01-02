/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012-2013 Anders Welén, anders@welen.net
 * 
 * This file is part of Buzz.
 *
 * Buzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Buzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buzz.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.welen.buzz.typehandler.dmr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.as.server.Services;

/**
 * Helper class to get a inVM reference to ModelControllerClient
 * 
 * Implementation "borrowed" from: http://management-platform.blogspot.se/2012/07/co-located-management-client-for.html
 * 
 * @author welle
 */
public class ManagementService implements ServiceActivator {

	private static volatile ModelController controller;
	private static volatile ExecutorService executor;

	// Inner class
	private static class GetModelControllerService implements Service<Void> {
		private InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();

		public Void getValue() throws IllegalStateException, IllegalArgumentException {
			return null;
		}

		public void start(StartContext context) throws StartException {
			ManagementService.executor = Executors.newFixedThreadPool(5, new ThreadFactory() {

				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("ManagementServiceModelControllerClientThread");
					return t;
				}

			});
			ManagementService.controller = modelControllerValue.getValue();
		}


		public void stop(StopContext context) {
			try {
				ManagementService.executor.shutdownNow();
			} finally {
				ManagementService.executor = null;
				ManagementService.controller = null;
			}
		}
	}

	public static ModelControllerClient getClient() {
		return controller.createClient(executor);
	}

	public void activate(ServiceActivatorContext context)	{	   
		final GetModelControllerService service = new GetModelControllerService();
		context.getServiceTarget()
			.addService(ServiceName.of("management", "client", "getter"), service)
			.addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.modelControllerValue)
			.install();
	}

}
