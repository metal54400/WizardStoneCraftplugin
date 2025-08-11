**Deepstone**:
repositories {
mavenCentral()
maven {
name = "papermc-repo"
url = "https://repo.papermc.io/repository/maven-public/"
}
maven {
name = "sonatype"
url = "https://oss.sonatype.org/content/groups/public/"
}
}

dependencies {
compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
}

**WizardStoneCraft**:
repositories {
mavenCentral()
maven {
name = "papermc-repo"
url = "https://repo.papermc.io/repository/maven-public/"
}
maven {
name = "sonatype"
url = "https://oss.sonatype.org/content/groups/public/"
}

    maven { url 'https://jitpack.io' }
    maven {
        url = 'https://repo.extendedclip.com/content/repositories/placeholderapi/'
    }
    maven {
        name "essentialsx-releases"
        url "https://repo.essentialsx.net/releases/"
    }
    maven {
        url "https://repo1.maven.org/maven2/xyz/jpenilla/squaremap-api/1.3.4/squaremap-api-1.3.4.pom"
    }
    maven { url 'https://maven.maxhenkel.de/repository/public' }

    maven {
        url = uri("https://repo.maxhenkel.de/repository/maven-public/")
    }

}

dependencies {
compileOnly 'de.maxhenkel.voicechat:voicechat-api:2.5.27'
compileOnly 'net.luckperms:api:5.4'
compileOnly 'me.clip:placeholderapi:2.11.6'
compileOnly("xyz.jpenilla:squaremap-api:1.3.4")
compileOnly ("com.github.booksaw:BetterTeams:4.10.0")
compileOnly ("com.github.GriefPrevention:GriefPrevention:17.0.0")
compileOnly 'com.github.NEZNAMY:TAB-API:5.2.5'
compileOnly('com.github.MilkBowl:VaultAPI:1.7')
compileOnly 'io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT'
compileOnly('net.essentialsx:EssentialsX:2.21.0')
compileOnly (files ('libs/bloodmoon.jar') )
compileOnly (files ('libs/voicechat-bukkit-2.5.27.jar') )
compileOnly (files ('libs/Duels-3.5.3.jar') )
compileOnly(files('libs/GravesX-4.9.7.3.jar'))
compileOnly(files('libs/GSit-2.4.2.jar'))
compileOnly(files('libs/ProtocolLib.jar'))
compileOnly(files('libs/EconomyShopGUI-Premium-5.21.0.jar'))
compileOnly(files('libs/CrazyAuctions-1.6.2.jar'))
compileOnly(files('libs/EssentialsXChat-2.21.1.jar'))
compileOnly(files('libs/Deepstone-0.0.0.7-1.21.5.jar'))
compileOnly(files('libs/beautyquests-1.0.5.jar'))

    testImplementation 'junit:junit:4.13.2'

}