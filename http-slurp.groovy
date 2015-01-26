@Grab(group='org.apache.camel', module='camel-core', version='2.13.1')
@Grab(group='org.apache.camel', module='camel-mail', version='2.13.1')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.7')

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import javax.activation.DataHandler

def username = "username@example.com"
def password = "password"

CamelContext context = new DefaultCamelContext()
context.addRoutes(new RouteBuilder() {
	public void configure() {

		from("imaps://imap.gmail.com?" // email domain
				+ "username=" + username
				+ "&password=" + password
				+ "&delete=false"
				+ "&unseen=true"
				+ "&consumer.delay=60000").process(new Processor() {

			public void process(Exchange exchange) {
				Map<String, DataHandler> attachments = exchange.getIn().getAttachments();

				if (attachments.size() > 0) {
					for (String name : attachments.keySet()) {
						DataHandler dh = attachments.get(name);

						// get the file name
						String filename = dh.getName();

						// get the content and convert it to byte[]
						byte[] data = exchange.getContext().getTypeConverter().convertTo(byte[].class, dh.getInputStream());

						// write the data to a file
						FileOutputStream out = new FileOutputStream(filename);
						out.write(data);
						out.flush();
						out.close();
					}
				}
			}
		})
	}
})

context.start()
addShutdownHook { context.stop() }
synchronized(this) { this.wait() }