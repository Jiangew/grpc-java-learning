gRPC Start Learning
=======================

This is a example for using [gRPC](http://www.grpc.io), which is a high performance, open-source universal RPC framework from Google.

This project demonstrates how to write gRPC server and client application using Java.

## How to Build

### Java Server & Client

To build this demo, run in this directory:
```sh
    $ ./gradlew installDist
```

This creates the scripts `MovieService-server`, `MovieService-client` in the
`build/install/grpc-start-learning/bin/` directory. Remember to run the server first before running the client.
```sh
    $ ./build/install/grpc-start-learning/bin/MovieService-server
```

Then in another terminal window,
```sh
    $ ./build/install/grpc-start-learning/bin/MovieService-client
```

That's it ...
