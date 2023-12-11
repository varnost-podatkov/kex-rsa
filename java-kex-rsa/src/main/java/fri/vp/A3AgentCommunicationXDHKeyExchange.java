package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

public class A3AgentCommunicationXDHKeyExchange {
    public static void main(String[] args) {

        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {

            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {

            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}
