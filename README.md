# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this project (see [Licensing](#licensing)), and acknowledge that I (the author of this project) have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License".


# ChatLog-Quilt

Just a chat logging mod, really!

This mod aims to seamlessly and undetectably log all chat messages that the client receives and maintain all data such as formatting and hover & click events (some extra functionality also included).

It is a primary goal of this to be entirely client-sided, unseen and uncontrollable by servers. As such, any requested changes (whether in the form of a feature request or pull request) which do not follow this will be rejected.

# FAQ

#### Q: I don't want people using this mod or some of its features on my server. Can you add a way for servers to disable specific modules of this mod?
A: Why would you care if your players log the chat? Also, you really didn't read the description above, did you? No part of this mod shall be visible to or controllable by any server.

#### Q: Can I use this in my modpack?
A: This mod is licensed under OSL-3.0. You may do as you wish with it so long as it does not claim to be "endorsed" or otherwise supported by the author(s) of this project unless otherwise stated, and follows the terms of the OSL-3.0 license (including, but not limited to: you must make the source code of all modifications you make to this mod available to all who you distribute the mod to).

#### Q: Can you add reach/killaura/scaffold/etc?
A: Why would I add that to a chat logging mod? That's clearly all this is. Additionally, these modifications would be visible to the server, which I don't allow for this project.

#### Q: Can you add some other feature that isn't visible to the server?
A: I will definitely consider it. Please submit a feature request or a pull request. So long as it is completely client-sided and isn't malicious to the end user I will likely add it.

#### Q: The chat logging got me banned and was visible to the server!
A: I provide no guarantee of functionality or safety, nor warranty, as per our license, so please don't yell at me. However, I do wish to fix this issue, as it means something this mod does is visible to the server, so please submit an issue report if this happens to you with the server in question, a full list of modifications, and their versions.

#### Q: Can you port this to Fabric?
A: I won't, but you're welcome to! I made this in Quilt because I wanted to use Quilt mappings and to be better compatible with Quilt mods. It should be relatively straightforward to port this to Fabric, but I just don't care to. Though, if someone submits a PR which does this, and I merge it, I will maintain it.

#### Q: Will you port this to Forge/some other modloader?
A: I have no plans to as of now. Quilt (and Fabric) currently do not send a mod list to the server, and I wish to log chat in peace. I have no interest in circumventing the mod list sent to the server in Forge or any other modloader, so this will remain Quilt for the time being. However, you are welcome to submit a pull request with support so long as all modifications are invisible to the server. This will be your responsibility to maintain.

#### Q: Will you port to version X.X.X?
A: Unlike some other developers I have full intention to support older versions. It is worth noting that it will still be in Quilt, which may be problematic for those who use 1.8 where Forge is still the only usable option as of writing this, but we do have plans to support it in the future (don't ask for an ETA, it will happen when it will happen. The progress will be visible in the version's corresponding branch and you are welcome to contribute to it to speed up the process).

#### Q: There seem to be some non-chat logging features in this mod...
A: It's just a chat logging mod, really!

#### Q: What kind of weird license is this?
A: OSL-3.0 is the closest equivalent to a LAGPL I could find. AGPL and GPL are incompatible with Minecraft, and LGPL doesn't protect network use. OSL-3.0 protects network use and is compatible with Minecraft.

#### Q: Why though? It's so strict!!!!
A: This is and will remain free, copyleft software. Any requests to change the license other than to make it even stronger will be denied immediately (unfortunately GPL and AGPL aren't comaptible with Minecraft due to linking restrictions, as much as I'd like to use them). Even in situations where I use parts of other projects with more "permissive" licenses, I will treat them as copyleft, free software.

# <a name="licensing" />Licensing

This project is licensed under OSL-3.0. Some parts of this project were taken from other, non-OSL-3.0 (however still "permissive", so, therefore not violating any licenses) projects. All changes from the original versions of the code from these projects is OSL-3.0, but the original versions are still the copyright of their original authors. These features and the source code used in the creation of them are listed below:

NOTE: for all references there is a specific commit included. No code past this point was used in this project. All pull requests must update this list for licensing reasons. Any changes made after this point (including changes in license) do not apply to this project unless this file is updated to reflect that. If code past this point is based on code from this project (excluding unmodified code from the originating project), they are required to make their project (or the parts which use ChatLog code; see OSL-3.0 terms) OSL-3.0.

### Perspective Module

##### Perspective Mod Redux

Reference: https://github.com/BackportProjectMC/PerspectiveModRedux/tree/bf423a398ccef3a94e8fcd68ea02fc91ae94b6b3

License: Expat (The Unlicense)

Author: BackportProjectMC

### Anti-Fog Module

##### Clear Skies

Reference: https://github.com/grondag/clear-skies/tree/009689f4aa429730ac91a9e8b7f96d31d5ec1821

License: Apache-2.0

Author: grondag

### Config Screen

##### Roughly Enough Items

Reference: https://github.com/shedaniel/RoughlyEnoughItems/tree/12dac7bb9c85da1c8c2ff28a06b8c7406c0c6611

License: Expat (MIT)

Author: shedaniel

### Rendering System

##### SeedCrackerX

Reference: https://github.com/19MisterX98/SeedcrackerX/tree/b496facd0e89a1f86668a2cd3ad3182dea906edf

License: Expat (MIT)

Author: 19MisterX98
