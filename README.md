# WizardStoneCraftplugin

    Ce Plugin est libre de service 

Developper api:

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
    maven { url 'https://jitpack.io' }
    }

    dependencies {
    compileOnly 'net.luckperms:api:5.4'
    compileOnly 'me.clip:placeholderapi:2.11.6'
    compileOnly("xyz.jpenilla:squaremap-api:1.3.4")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly "com.github.MilkBowl:VaultAPI:1.7"
    compileOnly ("com.github.booksaw:BetterTeams:4.10.0")
    }


