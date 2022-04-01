#!/usr/bin/env python
import html
import itertools
import json
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
    data_path = pathlib.Path.home() / 'data' / 'ashes-bot' / 'event'
    dat_files = [x for x in list(pathlib.Path(data_path).glob('*.dat'))]
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


# so this doesn't work
# need to somehow know how long they have been offline for before adding a guild level to the list
def enough_time(previous_time, current_time):
    prev = datetime.strptime(previous_time, '%Y-%m-%d %H:%M:%S')
    curr = datetime.strptime(current_time, '%Y-%m-%d %H:%M:%S')
    seconds = (curr - prev).seconds
    print(f'{seconds} seconds passed since last level')
    return seconds > 1


def get_data():
    world_url = 'https://api.tibiadata.com/v3/world/Nefera'
    guild_url = 'https://api.tibiadata.com/v3/guild/Ashes+Remain'

    world_json = requests.get(world_url).json()
    guild_json = requests.get(guild_url).json()

    tracked_ranks = ['Incinerate', 'Phoenix', 'Hellbringer', 'Hellblaze', 'Firestorm', 'Wildfire', 'Flame']

    try:
        all_guild_chars = guild_json['guilds']['guild']['members']
        guild_chars = [g for g in guild_chars if g['rank'] in tracked_ranks]
    except:
        guild_chars = []
    world_chars = world_json['worlds']['world'].get('online_players', [])

    html_unescape(guild_chars)
    html_unescape(world_chars)

    return(guild_chars, world_chars)


def html_unescape(l):
    for i in l:
        i['name'] = html.unescape(i['name'])


if __name__ == '__main__':
    main()
