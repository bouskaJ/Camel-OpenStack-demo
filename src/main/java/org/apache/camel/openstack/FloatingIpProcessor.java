package org.apache.camel.openstack;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;

/**
 * Processor which add floating IP to servers
 */
public class FloatingIpProcessor implements Processor {

	@Override
	public void process(Exchange e) throws Exception {
		final OSClient os = createClient();

		// first we need to associate new floating IP from pool
		FloatingIP ip = createClient().compute().floatingIps().allocateIP("public");
		Server s = e.getIn().getBody(Server.class);

		// wait until server is Active
		while (!Server.Status.ACTIVE.equals(s.getStatus())) {
			Thread.sleep(500);
			s = os.compute().servers().get(s.getId());
		}

		// associate floating IP with the node
		os.compute().floatingIps().addFloatingIP(s, ip.getFloatingIpAddress());

		// set headers
		e.getIn().setHeader("server_ip", ip.getFloatingIpAddress());
		e.getIn().setHeader("server_name", s.getName());
	}

	private OSClient createClient() {
		IOSClientBuilder.V3 builder = OSFactory.builderV3().endpoint(MyRouteBuilder.OPENSTACK_ENDPOINT);

		builder.credentials(MyRouteBuilder.OPENSTACK_USER, MyRouteBuilder.OPENSTACK_PASSWORD,
				Identifier.byId("default"));

		builder.scopeToProject(Identifier.byId(MyRouteBuilder.OPENSTACK_PROJECT_ID));

		return builder.authenticate();
	}
}
