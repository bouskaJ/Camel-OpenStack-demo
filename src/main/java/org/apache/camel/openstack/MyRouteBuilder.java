package org.apache.camel.openstack;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

	public static final String OPENSTACK_ENDPOINT = "http://172.24.1.69:5000/v3";
	public static final String OPENSTACK_PASSWORD = "nomoresecret";
	public static final String OPENSTACK_USER = "admin";
	public static final String OPENSTACK_PROJECT_ID = "d387038c917d4e21bd83be4bb3e7a9eb";

	/**
	 * Let's configure the Camel routing rules using Java code...
	 */
	public void configure() {

		// this route gets configuration from file resources
		// see files in src/data
		from("file:src/data")

				// set required headers
				.setHeader("operation", constant("create"))
				.setHeader("name", xpath("/node/name/text()"))
				.setHeader("FlavorId", xpath("/node/flavorId/text()"))
				.setHeader("ImageId", xpath("/node/imageId/text()"))

				// create servers
				.to("openstack-nova://" + OPENSTACK_ENDPOINT + "?password=" + OPENSTACK_PASSWORD + "&username="
						+ OPENSTACK_USER + "&project=" + OPENSTACK_PROJECT_ID + "&subsystem=servers")

				// associate floating IP with servers
				// using wireTap -> asynchronous execution
				.wireTap("direct:floatingIp");

		// associate floating IP with node
		// see FloatingIpProcessor
		from("direct:floatingIp")
				.process(new FloatingIpProcessor())
				// wait unless IP address is properly assotiated with node
				.delayer(3000)
				.log("Floating IP ${header.server_ip} was assotiated with ${header.server_name}")
				.to("direct:ssh");

		// execute ssh command
		from("direct:ssh")
				.setBody(constant("echo $SSH_CONNECTION"))
				.toD("ssh:cirros:cubswin:)@${headers.server_ip}?")
				.log("SSH connection info: ${body}");
	}
}
