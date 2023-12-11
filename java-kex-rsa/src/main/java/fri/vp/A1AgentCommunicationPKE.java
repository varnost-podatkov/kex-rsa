package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

public class A1AgentCommunicationPKE {
    public static void main(String[] args) {

        final Environment env = new Environment();

        env.add(new Agent("ana") {
            @Override
            public void task() throws Exception {

            }
        });

        env.add(new Agent("bor") {
            @Override
            public void task() throws Exception {

            }
        });

        env.connect("ana", "bor");
        env.start();
    }
}
