# BareYoctoDistro
 A relatively barebones distribution of a Linux system built on Poky.

 In order to set this all up, you will need any Linux distribution. Can be set up in a VM (personally used Oracle VB).

## Guide

### Summary of Key Files Included

| Path                                             | Description                                      |
|--------------------------------------------------|--------------------------------------------------|
| `barebones-layer/recipes-core/images/barebones-image.bb`        | Custom recipe for thje image.              |
| `poky/build/conf/local.conf`                     | Build configuration for settings and preferences.|
| `poky/build/conf/bblayers.conf`                  | List of layers used in the build.                |

### Initialization

Open the terminal. Update your package list first:

```bash
sudo apt-get update
```

and install the required packages for building images with Yocto:


```bash
sudo apt-get install gawk wget git-core diffstat unzip texinfo gcc-multilib \
build-essential chrpath socat cpio python3 python3-pip python3-pexpect \
xz-utils debianutils iputils-ping libsdl1.2-dev xterm \
curl lz4 zstd
```

Create a working directory:

```bash
mkdir ~/yocto
cd ~/yocto
```

### Repo Cloning

Clone the Yocto Project's reference distribution, Poky. Use the scarthgap branch - this is the most recent release - 4.2.

```bash
git clone -b scarthgap git://git.yoctoproject.org/poky.git
```

Since RPi5 is embedded, we will need to use BitBake layers from `openembedded` for this:

```bash
git clone -b scarthgap git://git.openembedded.org/meta-openembedded
```

```bash
git clone -b scarthgap git://git.yoctoproject.org/meta-raspberrypi
```

After duplication, `meta-raspberrypi` requires some modifications in order to adequately support u-boot. Currently, RPi5 does not have full support of U-Boot, so if you want this to run regardless, you will need to modify the following files:
`..\meta-raspberrypi\conf\machine\raspberrypi5.conf`, 
`..\meta-raspberrypi\recipes-bsp\u-boot\u-boot_%.bbappend`

Please use the files in this repo to modify these files accordingly.

### Build Environment & Configurations

Navigate to the Poky directory and initialize the build environment:

```bash
cd poky
source oe-init-build-env
```

This will create the `build` directory which is where all the build files will go, and sets up all the require `env` variables. With all the new layers downloaded and the build environment set up, it is time for configurations.

Edit `conf/bblayers.conf` to include all the necessary layers:

```bash
nano conf/bblayers.conf
```

Make sure that your `BBLAYERS` looks like this:

```
BBLAYERS ?= " \
  /home/vboxuser/yocto/poky/meta \
  /home/vboxuser/yocto/poky/meta-poky \
  /home/vboxuser/yocto/poky/meta-yocto-bsp \
  /home/vboxuser/yocto/meta-openembedded/meta-oe \
  /home/vboxuser/yocto/meta-openembedded/meta-python \
  /home/vboxuser/yocto/meta-openembedded/meta-networking \
  /home/vboxuser/yocto/meta-openembedded/meta-multimedia \
  /home/vboxuser/yocto/meta-openembedded/meta-initramfs \
  /home/vboxuser/yocto/meta-raspberrypi \
  /home/vboxuser/yocto/barebones-layer \ # This must be added AFTER the creation of the layer. Please come back to this after the future step.
"
```

You may also use the file provided in this repo.

#### Set the Target Machine

Edit `conf/local.conf` to set the machine to Raspberry Pi 5, as well as add some Mender configurations. Use the file provided on the GitHub. In the end, the file should look like so:

```
...

MACHINE ?= "raspberrypi5"

DISTRO_FEATURES:append = " systemd"
DISTRO_FEATURES_BACKFILL_CONSIDERED = "sysvinit"
VIRTUAL-RUNTIME_init_manager = "systemd"

EXTRA_IMAGE_FEATURES += "ssh-server-openssh"
```

#### Create the Custom Layer

From the `build` directory:

```bash
bitbake-layers create-layer ../barebones-layer
```

Return to the BBLAYERS configuration step and add the custom layer there.

#### Create a Custom Image Recipe

In your barebones layer, create a custom image recipe. This will dictate which packages the image should contain.

```bash
mkdir -p ../barebones-layer/recipes-core/images
```

Create and modify `barebones-layer\recipes-core\images\barebones-image.bb` to contain the file that is on this repo. It should look like this:

```
SUMMARY = "Barebones Minimal Image"
DESCRIPTION = "A minimal image for Raspberry Pi 5 with required packages"
LICENSE = "MIT"

IMAGE_INSTALL += " \
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

inherit core-image
```

#### Start Baking

Initiate the bitbake like so:

```bash
bitbake barebones-image
```

The initial build may take several hours, depending on the performance of your PC/VM. After a successful build, the image should be located here:

```
tmp/deploy/images/raspberrypi5/barebones-image-raspberrypi5.wic.gz
```

After this, you should flush the image to your SD Card using `bmap-tools` or similar:

```bash
cd poky/build
sudo bmaptool copy tmp/deploy/images/raspberrypi5/barebones-image-raspberrypi5.rootfs-20241010214339.wic.bz2 /dev/sdb # FLASH DRIVE
```
