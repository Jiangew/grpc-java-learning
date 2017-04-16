package com.jiangew.movieService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 为了调用服务端的方法，需要创建 stub。有两种类型的 stub：
 * 1.blocking/synchronous stub：阻塞，客户端发起 RPC 调用后一直等待服务端的响应
 * 2.non-blocking/asynchronous stub：非阻塞，异步响应，通过 StreamObserver 在响应时进行回调
 * <p>
 * Author: Jiangew
 * Date: 21/03/2017
 */
public class MovieServiceClient {
    private static final Logger logger = Logger.getLogger(MovieServiceClient.class.getName());

    private final ManagedChannel channel;
    private final MovieServiceGrpc.MovieServiceBlockingStub blockingStub;
    private final MovieServiceGrpc.MovieServiceStub asyncStub;

    /**
     * 为了创建 stub，首先要创建 channel，需要指定服务端的主机和监听的端口。然后按序创建阻塞或者非阻塞的 stub。
     *
     * @param host
     * @param port
     */
    public MovieServiceClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true)
                .build();

        blockingStub = MovieServiceGrpc.newBlockingStub(channel);
        asyncStub = MovieServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 通过 stub 来发起 RPC 调用，直接在 stub 上调用同名方法
     */
    public void getMovieList() {
        logger.info("call listMovies() method : ");
        ActorId request = ActorId.newBuilder().setId(1).build();
        MovieList movieList = blockingStub.listMovies(request);
        for (Movie movie : movieList.getMoviesList()) {
            logger.info(movie.toString());
        }
        logger.info("finished!");
    }

    /**
     * 对于 stream Song 的响应，返回的是一个迭代器 Iterator<Movie>
     */
    public void getMoviesUsingStream() {
        logger.info("call getMovies() method : ");
        ActorId request = ActorId.newBuilder().setId(1).build();
        Iterator<Movie> iterator = blockingStub.getMovies(request);
        while (iterator.hasNext()) {
            logger.info(iterator.next().toString());
        }
        logger.info("finished!");
    }

    /**
     * 对于异步的 stub，则需要一个 StreamObserver 对象来完成回调处理：
     * 1.创建了一个实现了 StreamObserver 接口的匿名内部类对象 responseObserver 用于回调处理，
     * 每一次在收到一个响应的 Song 对象时会触发 onNext() 方法，RPC 调用完成或出错时则会调用 onCompleted() 和 onError()。
     * 2.使用了一个 CountDownLatch，等待响应全部接受完毕后才从方法返回。
     *
     * @throws InterruptedException
     */
    public void getMoviesUsingAsyncStub() throws InterruptedException {
        logger.info("call getMovies() method using asynchronous stub : ");
        ActorId request = ActorId.newBuilder().setId(1).build();
        final CountDownLatch latch = new CountDownLatch(1); // using CountDownLatch

        StreamObserver<Movie> responseObserver = new StreamObserver<Movie>() {
            @Override
            public void onNext(Movie value) {
                logger.info("get movie :" + value.toString());
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                logger.info("failed with status : " + status);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("finished!");
                latch.countDown();
            }
        };

        asyncStub.getMovies(request, responseObserver);
        latch.await();
    }

    public static void main(String[] args) throws InterruptedException {
        MovieServiceClient client = new MovieServiceClient("localhost", 50051);
        try {
            client.getMovieList();
            client.getMoviesUsingStream();
            client.getMoviesUsingAsyncStub();
        } finally {
            client.shutdown();
        }
    }

}
