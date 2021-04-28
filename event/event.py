#!/usr/bin/env python
import itertools
import os
import pathlib
import requests

from datetime import datetime
from nested_lookup import nested_lookup

def main():
    (guild_chars, world_chars) = get_data()

    levels = []
    for char in guild_chars:
        level = get_level(char, world_chars)
        levels.append({'name': char['name'], 'level': level})

    update_files(levels)

    print('done')


def update_files(levels):
    script_path = os.path.dirname(os.path.realpath(__file__))
    dat_files = [x for x in list(pathlib.Path(script_path).glob('*.dat'))]
    date_time = datetime.now().isoformat(' ', 'seconds')

    for dat_file in dat_files:
        with open(dat_file) as f:
            lines = f.read().splitlines()
        lines.reverse()

        with dat_file.open('a') as f:
            for level in levels:
                previous_level = next((i for i in lines if i.split(',')[1] == level['name']), None)
                current_level = level['level']
                if (not previous_level) or \
                    (int(previous_level.split(',')[2]) != current_level
                    and enough_time(previous_level.split(',')[0], date_time)):
                    name = level['name']
                    next_line = f'{date_time},{name},{current_level}'
                    print(next_line)
                    f.write(f'{next_line}\n')


def get_level(guild_char, world_chars):
    # We check levels on the world list because the guild level is only updated when you log off
    guild_level = guild_char['level']
    world_level = [x['level'] for x in world_chars if x['name'] == guild_char['name']]
    if world_level: return world_level[0]
    else: return guild_level


# We don't update the level if it was already updated in the last 5 minutes
# This is to prevent caching issues where the someone logs off and the guild data has
# not updated yet, so if thinks they are back to their original level when they first logged in
def enough_time(previous_time, current_time):
    prev = datetime.datetime.strptime(previous_time, '%Y-%m-%d %H:%M:%S')
    curr = datetime.datetime.strptime(current_time, '%Y-%m-%d %H:%M:%S')
    seconds = (curr - prev).seconds
    print('f{seconds} seconds passed since last level')
    return seconds > 300


def get_data():
    world_url = 'https://api.tibiadata.com/v2/world/Nefera.json'
    guild_url = 'https://api.tibiadata.com/v2/guild/Ashes+Remain.json'

    world_json = requests.get(world_url).json()
    guild_json = requests.get(guild_url).json()

    # TODO exclude low rank members and leader alts
    guild_members = guild_json['guild']['members'][2:]
    guild_chars = list(itertools.chain(*nested_lookup('characters', guild_members)))
    world_chars = world_json['world'].get('players_online', [])

    return(guild_chars, world_chars)


if __name__ == '__main__':
    main()
