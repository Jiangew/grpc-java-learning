package com.jiangew.movieService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 服务端代码的实现主要分为两部分：
 * 1.实现服务接口需要完成的实际工作：主要通过继承生成的基本服务类，并重写相应的 RPC 方法来完成具体的工作
 * 2.运行一个 gRPC 服务，监听客户端的请求并返回响应
 * <p>
 * Author: Jiangew
 * Date: 21/03/2017
 */
public class MovieServiceServer {
    private static final Logger logger = Logger.getLogger(MovieServiceServer.class.getName());

    /* The port on which the server should run */
    private int port = 50051;
    private Server server;

    /**
     * 启动服务监听
     * 使用 ServerBuilder 来创建一个 Server，主要分为三步：
     * 1.指定服务监听的端口
     * 2.创建具体的服务对象，并注册给 ServerBuilder
     * 3.创建 Server 并启动
     *
     * @throws IOException
     */
    private void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(new MovieServiceImpl()).build();
        server.start();
        logger.info("server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                MovieServiceServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final MovieServiceServer server = new MovieServiceServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * 自定义一个内部类，继承自生成的 JRServiceGrpc.JRServiceImplBase 抽象类，重写服务方法来完成具体的工作
     */
    private static class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {

        /**
         * @param request          请求
         * @param responseObserver 用户处理请求和关闭通道
         */
        @Override
        public void listMovies(ActorId request, StreamObserver<MovieList> responseObserver) {
            MovieList list = MovieList.newBuilder().addAllMovies(genMovies(request)).build();
            // 返回相应
            responseObserver.onNext(list);
            responseObserver.onCompleted();
        }

        @Override
        public void getMovies(ActorId request, StreamObserver<Movie> responseObserver) {
            List<Movie> movies = genMovies(request);
            for (Movie movie : movies) {
                // 多次返回相应
                responseObserver.onNext(movie);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    responseObserver.onError(e);
                }
            }
            responseObserver.onCompleted();
        }

        private List<Movie> genMovies(ActorId request) {
            Actor actor = Actor.newBuilder().setId(request.getId()).setName("Jiangew").build();
            List<Movie> movies = new ArrayList<>();
            movies.add(Movie.newBuilder().setId(1).setName("True Love True Life").setActor(actor).build());
            movies.add(Movie.newBuilder().setId(2).setName("Today is a Good Day").setActor(actor).build());
            movies.add(Movie.newBuilder().setId(3).setName("One Year Ago").setActor(actor).build());

            return movies;
        }
    }

}
