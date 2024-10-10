SUMMARY = "Barebones Minimal Image"
DESCRIPTION = "A minimal image for Raspberry Pi 5 with required packages"
LICENSE = "MIT"

IMAGE_INSTALL += " \
    packagegroup-core-boot \
    packagegroup-core-ssh-openssh \
    libxcrypt-compat \
    psplash \
    networkmanager \
    cantarell-fonts \
    glibc \
    libatomic-ops \
    python3 \
    plymouth \
    python3-requests \
    bash \
    openssh \
    raspi-gpio \
    rpi-gpio \
    userland \
    rsyslog \
    sudo \
    nano \
    libcamera \
"

DISTRO_FEATURES:append = " usrmerge systemd"
VIRTUAL-RUNTIME_init_manager = "systemd"
DISTRO_FEATURES_BACKFILL_CONSIDERED = "sysvinit"

EXTRA_IMAGE_FEATURES += "ssh-server-openssh"

inherit core-image