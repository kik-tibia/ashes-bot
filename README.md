# Ashes Bot

Discord bot for the Ashes Remain Tibia guild

# Dependencies
- Docker
- Python 3
  - nested-lookup

# Deployment steps

Build docker image  
1. `cd ashes-bot`
1. `sbt docker:publishLocal`

Copy to server  
1. `docker images`
1. `docker save <image_id> | bzip2 | ssh bots docker load`

On the server
1. Create an env file with the missing data from `src/main/resources/application.conf`
1. Ensure that the directory structure is setup correctly: `mkdir -p $HOME/data/ashes-bot/{event,members}`
1. Create members file and give it permissions that docker can write to: `touch $HOME/data/ashes-bot/members/` `chmod 666 $HOME/data/ashes-bot/members/ashes-remain.dat`
1. Run the docker container, pointing to the env file created in step 1: `docker run --rm -d --env-file prod.env -v $HOME/data/ashes-bot:$HOME/data/ashes-bot <image_id>`
1. Run the event update loop: `cd event` `while true; do timeout 60 ./event.py ; sleep 60; done`
