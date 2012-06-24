@echo off
set mylyn_contexts=D:\Mylyn Metrics\Mylyn contexts
set project_metrics=D:\Mylyn Metrics\Projects
set frameworks_path=D:\Mylyn Metrics\Frameworks
set frameworks=%FRAMEWORKS_PATH%\aether-core.xml,%FRAMEWORKS_PATH%\aether-loader.xml,%FRAMEWORKS_PATH%\cloudloop-adapter.xml,%FRAMEWORKS_PATH%\cloudloop-main.xml,%FRAMEWORKS_PATH%\dasein-aws.xml,%FRAMEWORKS_PATH%\dasein-core.xml,%FRAMEWORKS_PATH%\dasein-google.xml,%FRAMEWORKS_PATH%\jclouds-aws-common.xml,%FRAMEWORKS_PATH%\jclouds-aws-s3.xml,%FRAMEWORKS_PATH%\jclouds-blobstore.xml,%FRAMEWORKS_PATH%\jclouds-core.xml,%FRAMEWORKS_PATH%\jclouds-s3.xml,%FRAMEWORKS_PATH%\jets3t.xml,%FRAMEWORKS_PATH%\libcloud.xml

set project=neoedit-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-aether-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-cloudloop
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-cloudloop-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-cloudloop-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-dasein
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-dasein-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-dasein-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jclouds
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jclouds-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jclouds-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jets3t
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jets3t-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-jets3t-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-libcloud
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-libcloud-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=neoedit-libcloud-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt

set project=jfilesync-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-aether-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-cloudloop
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-cloudloop-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-cloudloop-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-dasein
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-dasein-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-dasein-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jclouds
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jclouds-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jclouds-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jets3t
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jets3t-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-jets3t-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-libcloud
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-libcloud-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=jfilesync-libcloud-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt

set project=MuCommanderApp-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-aether-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-cloudloop
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-cloudloop-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-cloudloop-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-dasein
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-dasein-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-dasein-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jclouds
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jclouds-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jclouds-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jets3t
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jets3t-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-jets3t-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-libcloud
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-libcloud-migracion
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt
set project=MuCommanderApp-libcloud-to-aether
java -jar metricanator.jar "%MYLYN_CONTEXTS%\%PROJECT%.xml" "%PROJECT_METRICS%\%PROJECT%.xml,%FRAMEWORKS%" 1> %PROJECT%.results.txt 2> %PROJECT%.error.txt



