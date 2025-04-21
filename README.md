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
1. Member update loop
    1. Ensure that the directory structure is setup correctly: `mkdir -p $HOME/data/ashes-bot/{event,members}`
    1. Set up python environment with the required packages: `python -m venv .venv`, `source .venv/bin/activate`, `python -m pip install nested-lookup requests`
    1. Create members file: `touch $HOME/data/ashes-bot/members/ashes-remain.dat`
    1. Run the event update loop: `cd event` `while true; do timeout 60 ./event.py ; sleep 60; done`
1. Docker container
    1. Create an env file with the missing data from `src/main/resources/application.conf`
    1. Run the docker container, pointing to the env file created in step 1, changing user id if necessary: `docker run -d --env-file prod.env -v $HOME/data/ashes-bot:$HOME/data/ashes-bot --user $(id -u tom):$(id -g tom) -e JAVA_OPTS="-Xms192m -Xmx320m" --name ashes-bot ashes-bot:1.0.0`
