package mock;

/*

 Copyright 2018 hexosse

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class FakeServer {

    // Singleton
    private static FakeServer instance;

    // FakeServer
    private Server server;
    private Logger logger;


    // return {@link Server} instance
    public static Server get() {
        if(instance == null) {
            instance = new FakeServer();
            instance.setUp();
        }
        return instance.server;
    }

    // Private constructor, use static method get instead
    private FakeServer() {
        logger = Logger.getLogger("Fake-Server-Logger");
    }

    // Initialise the mocked {@link FakeServer} class
    public void setUp() {
        //
        server = mock(Server.class);
        //
        when(server.getName()).thenReturn("FakeServer");
        when(server.getVersion()).thenReturn("1.0.0");
        when(server.getBukkitVersion()).thenReturn("1.12");
        when(server.getLogger()).thenReturn(logger);
        when(server.isPrimaryThread()).thenReturn(true);

        // Inject this fake server
        Bukkit.setServer(server);
    }
}
