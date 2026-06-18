FROM debian:stable

RUN set -x && apt-get update && apt-get install -y unzip automake build-essential curl file pkg-config git python-is-python3 libtool flex bison

WORKDIR /opt/android

## INSTALL ANDROID NDK
ENV ANDROID_NDK_REVISION r27c
ENV ANDROID_NDK_HASH 090e8083a715fdb1a3e402d0763c388abb03fb4e
RUN set -x \
    && curl -O https://dl.google.com/android/repository/android-ndk-${ANDROID_NDK_REVISION}-linux.zip \
    && echo "${ANDROID_NDK_HASH}  android-ndk-${ANDROID_NDK_REVISION}-linux.zip" | sha1sum -c \
    && unzip android-ndk-${ANDROID_NDK_REVISION}-linux.zip \
    && rm -f android-ndk-${ANDROID_NDK_REVISION}-linux.zip

ENV WORKDIR /opt/android
ENV ANDROID_NDK_ROOT ${WORKDIR}/android-ndk-${ANDROID_NDK_REVISION}
ENV ANDROID_TOOLCHAIN ${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64
ENV PREFIX /opt/android/prefix
ENV ANDROID_API 21

ENV CC  armv7a-linux-androideabi21-clang
ENV CXX armv7a-linux-androideabi21-clang++
ENV AR  ${ANDROID_TOOLCHAIN}/bin/llvm-ar
ENV RANLIB ${ANDROID_TOOLCHAIN}/bin/llvm-ranlib
ENV STRIP  ${ANDROID_TOOLCHAIN}/bin/llvm-strip

#INSTALL cmake
ARG CMAKE_VERSION=3.31.12
ARG CMAKE_HASH=0dc2e9a6860f06bf10bd8fadc03e35d9eeb4df46e33763a7e480e987758f385c
RUN set -x \
    && cd /usr \
    && curl -L -O https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz \
    && echo "${CMAKE_HASH}  cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz" | sha256sum -c \
    && tar -xzf /usr/cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz \
    && rm -f /usr/cmake-${CMAKE_VERSION}-linux-x86_64.tar.gz
ENV PATH /usr/cmake-${CMAKE_VERSION}-linux-x86_64/bin:${ANDROID_TOOLCHAIN}/bin:$PATH

ARG NPROC=4

## Boost
ARG BOOST_VERSION=1.90.0
ARG BOOST_HASH=e848446c6fec62d8a96b44ed7352238b3de040b8b9facd4d6963b32f541e00f5
RUN set -x \
    && curl -L -f -o boost-${BOOST_VERSION}-b2-nodocs.tar.gz \
         https://github.com/boostorg/boost/releases/download/boost-${BOOST_VERSION}/boost-${BOOST_VERSION}-b2-nodocs.tar.gz \
    && echo "${BOOST_HASH}  boost-${BOOST_VERSION}-b2-nodocs.tar.gz" | sha256sum -c \
    && tar -xzf boost-${BOOST_VERSION}-b2-nodocs.tar.gz \
    && rm -f boost-${BOOST_VERSION}-b2-nodocs.tar.gz \
    && cd boost-${BOOST_VERSION} \
    && ./bootstrap.sh --prefix=${PREFIX} --without-icu \
    && echo "using clang : android : ${CXX} : <compileflags>--sysroot=${ANDROID_TOOLCHAIN}/sysroot <compileflags>-I${PREFIX}/include <linkflags>--sysroot=${ANDROID_TOOLCHAIN}/sysroot <linkflags>-L${PREFIX}/lib <linkflags>-liconv <archiver>${AR} <ranlib>${RANLIB} ;" > user-config.jam
ENV HOST_PATH $PATH

# Build iconv for lib boost locale
ENV ICONV_VERSION 1.15
ENV ICONV_HASH ccf536620a45458d26ba83887a983b96827001e92a13847b45e4925cc8913178
RUN set -x \
    && curl -O http://ftp.gnu.org/pub/gnu/libiconv/libiconv-${ICONV_VERSION}.tar.gz \
    && echo "${ICONV_HASH}  libiconv-${ICONV_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf libiconv-${ICONV_VERSION}.tar.gz \
    && rm -f libiconv-${ICONV_VERSION}.tar.gz \
    && cd libiconv-${ICONV_VERSION} \
    && ./configure --build=x86_64-linux-gnu --host=arm-linux-androideabi --prefix=${PREFIX} --disable-rpath --enable-static --disable-shared \
    && make -j${NPROC} && make install

## Build BOOST
RUN set -x \
    && cd boost-${BOOST_VERSION} \
    && ./b2 --build-type=minimal --user-config=user-config.jam link=static runtime-link=static \
         cflags=-fPIC cxxflags=-fPIC \
         --with-chrono --with-date_time --with-filesystem --with-program_options --with-regex \
         --with-serialization --with-system --with-thread --with-locale \
         --build-dir=android --stagedir=android \
         toolset=clang-android threading=multi threadapi=pthread target-os=android \
         -sICONV_PATH=${PREFIX} install -j${NPROC}

RUN ar rcs ${PREFIX}/lib/libboost_system.a

# download, configure and make Zlib
ENV ZLIB_VERSION 1.3.1
ENV ZLIB_HASH 9a93b2b7dfdac77ceba5a558a580e74667dd6fede4585b91eefb60f03b72df23
RUN set -x \
    && ( curl -fL -o zlib-${ZLIB_VERSION}.tar.gz https://github.com/madler/zlib/releases/download/v${ZLIB_VERSION}/zlib-${ZLIB_VERSION}.tar.gz \
      || curl -fL -o zlib-${ZLIB_VERSION}.tar.gz https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz \
      || curl -fL -o zlib-${ZLIB_VERSION}.tar.gz https://zlib.net/fossils/zlib-${ZLIB_VERSION}.tar.gz ) \
    && echo "${ZLIB_HASH}  zlib-${ZLIB_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf zlib-${ZLIB_VERSION}.tar.gz \
    && rm zlib-${ZLIB_VERSION}.tar.gz \
    && mv zlib-${ZLIB_VERSION} zlib \
    && cd zlib && ./configure --static \
    && make -j${NPROC}

# OpenSSL
ARG OPENSSL_VERSION=3.6.2
ARG OPENSSL_HASH=aaf51a1fe064384f811daeaeb4ec4dce7340ec8bd893027eee676af31e83a04f
RUN set -x \
    && curl -LO https://github.com/openssl/openssl/releases/download/openssl-${OPENSSL_VERSION}/openssl-${OPENSSL_VERSION}.tar.gz \
    && echo "${OPENSSL_HASH}  openssl-${OPENSSL_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf openssl-${OPENSSL_VERSION}.tar.gz \
    && rm openssl-${OPENSSL_VERSION}.tar.gz \
    && cd openssl-${OPENSSL_VERSION} \
    && ANDROID_NDK_ROOT=${ANDROID_NDK_ROOT} ./Configure android-arm \
           -D__ANDROID_API__=${ANDROID_API} \
           -fPIC \
           -static \
           no-shared no-tests \
           --with-zlib-include=${WORKDIR}/zlib/include --with-zlib-lib=${WORKDIR}/zlib/lib \
           --prefix=${PREFIX} --openssldir=${PREFIX} \
    && make -j${NPROC} \
    && make install_sw

# ZMQ
ARG ZMQ_VERSION=v4.3.4
ARG ZMQ_HASH=4097855ddaaa65ed7b5e8cb86d143842a594eebd
RUN set -x \
    && git clone https://github.com/zeromq/libzmq.git -b ${ZMQ_VERSION} \
    && cd libzmq \
    && test `git rev-parse HEAD` = ${ZMQ_HASH} || exit 1 \
    && ./autogen.sh \
    && ./configure --prefix=${PREFIX} --host=arm-linux-androideabi --enable-static --disable-shared --without-libsodium --disable-curve --without-documentation \
    && make -j${NPROC} \
    && make install

# Sodium
ARG SODIUM_VERSION=1.0.22
ARG SODIUM_HASH=adbdd8f16149e81ac6078a03aca6fc03b592b89ef7b5ed83841c086191be3349
RUN set -x \
    && curl -LO https://download.libsodium.org/libsodium/releases/libsodium-${SODIUM_VERSION}.tar.gz \
    && echo "${SODIUM_HASH}  libsodium-${SODIUM_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf libsodium-${SODIUM_VERSION}.tar.gz \
    && rm libsodium-${SODIUM_VERSION}.tar.gz \
    && cd libsodium-${SODIUM_VERSION} \
    && ./autogen.sh \
    && ./configure --prefix=${PREFIX} --host=arm-linux-androideabi --enable-static --disable-shared --with-pic \
    && make -j${NPROC} \
    && make install

# libexpat (required by libunbound)
ARG LIBEXPAT_VERSION=2.7.5
ARG LIBEXPAT_TAG=R_2_7_5
ARG LIBEXPAT_HASH=9931f9860d18e6cf72d183eb8f309bfb96196c00e1d40caa978e95bc9aa978b6
RUN set -x \
    && curl -LO https://github.com/libexpat/libexpat/releases/download/${LIBEXPAT_TAG}/expat-${LIBEXPAT_VERSION}.tar.gz \
    && echo "${LIBEXPAT_HASH}  expat-${LIBEXPAT_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf expat-${LIBEXPAT_VERSION}.tar.gz \
    && rm expat-${LIBEXPAT_VERSION}.tar.gz \
    && cd expat-${LIBEXPAT_VERSION} \
    && ./buildconf.sh \
    && ./configure --prefix=${PREFIX} --host=arm-linux-androideabi --enable-static --disable-shared --with-pic \
    && make -j${NPROC} \
    && make install

# libunbound
ARG LIBUNBOUND_VERSION=1.24.2
ARG LIBUNBOUND_HASH=44e7b53e008a6dcaec03032769a212b46ab5c23c105284aa05a4f3af78e59cdb
RUN set -x \
    && ( curl -fL --retry 3 --retry-connrefused -o unbound-${LIBUNBOUND_VERSION}.tar.gz https://www.nlnetlabs.nl/downloads/unbound/unbound-${LIBUNBOUND_VERSION}.tar.gz \
      || curl -fL --retry 3 --retry-connrefused -o unbound-${LIBUNBOUND_VERSION}.tar.gz https://distfiles.macports.org/unbound/unbound-${LIBUNBOUND_VERSION}.tar.gz ) \
    && echo "${LIBUNBOUND_HASH}  unbound-${LIBUNBOUND_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf unbound-${LIBUNBOUND_VERSION}.tar.gz \
    && rm unbound-${LIBUNBOUND_VERSION}.tar.gz \
    && cd unbound-${LIBUNBOUND_VERSION} \
    && ./configure --prefix=${PREFIX} --host=arm-linux-androideabi --enable-static --disable-shared --with-pic \
         --disable-flto --with-ssl=${PREFIX} --with-libexpat=${PREFIX} \
    && make -j${NPROC} \
    && make install

# polyseed
RUN git clone https://github.com/tevador/polyseed.git
RUN set -x \
    && cd polyseed \
    && git reset --hard b7c35bb3c6b91e481ecb04fc235eaff69c507fa1 \
    && cmake -DCMAKE_INSTALL_PREFIX=${PREFIX} \
             -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK_ROOT}/build/cmake/android.toolchain.cmake \
             -DANDROID_ABI=armeabi-v7a \
             -DANDROID_PLATFORM=android-${ANDROID_API} \
             . \
    && make \
    && make install

# utf8proc
RUN git clone https://github.com/JuliaStrings/utf8proc -b v2.8.0
RUN set -x \
    && cd utf8proc \
    && git reset --hard 1cb28a66ca79a0845e99433fd1056257456cef8b \
    && mkdir build \
    && cd build \
    && rm -rf ../CMakeCache.txt ../CMakeFiles/ \
    && cmake -DCMAKE_INSTALL_PREFIX=${PREFIX} \
             -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK_ROOT}/build/cmake/android.toolchain.cmake \
             -DANDROID_ABI=armeabi-v7a \
             -DANDROID_PLATFORM=android-${ANDROID_API} \
             .. \
    && make \
    && make install

COPY . /src
RUN set -x \
    && cd /src \
    && env -u CC -u CXX CMAKE_INCLUDE_PATH="${PREFIX}/include" \
       CMAKE_LIBRARY_PATH="${PREFIX}/lib" \
       ANDROID_NDK_ROOT=${ANDROID_NDK_ROOT} \
       USE_SINGLE_BUILDDIR=1 \
       PATH=${HOST_PATH} make release-static-android-armv7-wallet_api -j${NPROC}

RUN set -x \
    && cd /src/build/release \
    && find . -path ./lib -prune -o -name '*.a' -exec cp '{}' lib \;
