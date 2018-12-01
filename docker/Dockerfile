FROM openjdk:8u181-alpine3.8

RUN apk update && apk upgrade && \
    apk add --no-cache bash wget curl nano git shadow sudo

################################################################################
# make this image compatible with IDE https://github.com/ai-traders/ide
# Normally, we do this in single line, but here we want the image to work without IDE too, hence cutom fix-uid-gid.sh
ENV IDE_VERSION=0.10.5
RUN git clone --depth 1 -b ${IDE_VERSION} https://github.com/ai-traders/ide.git /tmp/ide_git && \
  /tmp/ide_git/ide_image_scripts/src/install.sh && \
  rm -r /tmp/ide_git
COPY fix-uid-gid.sh /etc/ide.d/scripts/50-ide-fix-uid-gid.sh
################################################################################

ENTRYPOINT ["/usr/bin/entrypoint.sh"]
CMD ["/bin/bash"]

# ensure pretty bash prompt and env. variables are set
COPY bashrc /home/ide/.bashrc
COPY profile /home/ide/.profile

COPY gocd-json /usr/bin/gocd-json

RUN mkdir -p /usr/lib/gocd-json-plugin
COPY libs/json-config-plugin-*.jar /usr/lib/gocd-json-plugin/json-config-plugin.jar

ARG this_image_tag_arg
ENV this_image_tag=${this_image_tag_arg}
