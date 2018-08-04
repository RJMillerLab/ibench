########################################
# Derived from ubuntu distribution
########################################
FROM ubuntu:latest
########################################
# Create working directories
########################################
WORKDIR /ibench
########################################
# Install packages needed for building
########################################
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" >> /etc/apt/sources.list
RUN apt-get -y update && apt-get install -y \
	git \
	software-properties-common
########################################
# Install Java 8
########################################
ENV JAVA_VER 8
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer
########################################
# Install modern ant version
########################################
ENV ANT_VERSION 1.9.7
RUN mkdir -p /usr/share/ant && \
	cd && \
	wget -q http://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz && \
    tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz && \
    mv apache-ant-${ANT_VERSION}/ /usr/share/ant && \
	rm apache-ant-${ANT_VERSION}-bin.tar.gz && \
	ln -s /usr/share/ant/apache-ant-${ANT_VERSION}/bin/ant /usr/bin/ant
ENV ANT_HOME /usr/share/ant/apache-ant-${ANT_VERSION}/
ENV PATH ${PATH}:/usr/share/ant/apache-ant-${ANT_VERSION}/bin
########################################
# Clone iBench github repository
########################################
RUN git clone --depth 1 --recursive https://github.com/RJMillerLab/ibench.git /ibenchsrc
########################################
# Build and deploy iBench
########################################
RUN cd /ibenchsrc \
	&& ant jar-fat \
	&& cp -r /ibenchsrc/build/ibench/* /ibench
########################################
# Per default start ibench
########################################
ENTRYPOINT ["/ibench/ibench.sh"]
########################################
# Per default show help message
########################################
CMD ["-help"]
