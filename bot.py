#!/usr/bin/env python
import os
import discord
import pathlib
import itertools
import requests

from datetime import datetime
from discord.ext import commands
from discord.ext import tasks
from dotenv import load_dotenv
from nested_lookup import nested_lookup


load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')
new_joiner_channel = int(os.getenv('NEW_JOINER_CHANNEL'))
script_path = os.path.dirname(os.path.realpath(__file__))

bot = commands.Bot(command_prefix='!')


@bot.command(name='event', help='info: Provides an update on the level event.\nnew: Starts a new level event.')
async def event(ctx, arg=None):
    response = None

    if arg == 'info':
        response = info()
    elif arg == 'new':
        if 'Moderator' in [y.name for y in ctx.message.author.roles]:
            response = new_event()
        else:
            response = discord.Embed(title='Missing role', colour=0xffa32b, description='You need the Moderator role to run this command')

    if response:
        await ctx.send(embed=response)
    else:
        await ctx.send(f'Unknown argument **{arg}** to event command')


def info():
    ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
    latest = max(ids)
    dat_file = pathlib.Path(script_path) / 'event' / f'{latest}.dat'

    with open(dat_file) as f:
        levels = f.read().splitlines()
    rev_levels = levels.copy()
    rev_levels.reverse()

    chars = set(map(lambda x: x.split(',')[1], levels))

    char_data = []

    for char in chars:
        start_level_data = next(i for i in levels if i.split(',')[1] == char)
        end_level_data = next(i for i in rev_levels if i.split(',')[1] == char)
        start_level = int(start_level_data.split(',')[2])
        end_level = int(end_level_data.split(',')[2])
        char_data.append({'name': char, 'start_level': start_level, 'end_level': end_level})

    sorted_char_data = sorted(char_data, key=lambda x: x['start_level'])

    wildfire = []
    firestorm = []
    hellblaze = []
    phoenix = []
    hellbringer = []
    for char in sorted_char_data:
        name = char['name']
        start_level = char['start_level']
        end_level = char['end_level']
        gained = end_level - start_level
        if start_level < 300: rank = wildfire
        elif start_level < 400: rank = firestorm
        elif start_level < 500: rank = hellblaze
        elif start_level < 700: rank = phoenix
        else: rank = hellbringer

        if gained != 0:
            emoji = '' if gained > 0 else ''
            # TODO mention if they ranked up
            level_s = 'level' if gained == 1 else 'levels'
            rank.append(f'**{name}** gained **{gained}** {level_s}! ({start_level} to {end_level}){emoji}')

    # Temporary hack to get around discord size limit
    wildfire1, wildfire2 = split_list(wildfire)
    firestorm1, firestorm2 = split_list(firestorm)


    # TODO find the first x people who advanced y levels in each rank
    embed = discord.Embed(title='Level Event Update', colour=0xffa32b)
    if latest == 2:
        embed.add_field(name='Disclaimer', value='These are not the results of the April leveling event, this is a dummy event that started on Tuesday so that the bot can be tested in time for the actual May event!', inline=False)
    embed.add_field(name=':fire: Wildfire :fire: (1/2) - Two 2kk prizes for the first two people to level up 35 times', value=get_rank_info(wildfire1), inline=False)
    embed.add_field(name=':fire: Wildfire :fire: (2/2) - Two 2kk prizes for the first two people to level up 35 times', value=get_rank_info(wildfire2), inline=False)
    embed.add_field(name=':fire: Firestorm :fire: (1/2) - Two 2kk prizes for the first two people to level up 30 times', value=get_rank_info(firestorm1), inline=False)
    embed.add_field(name=':fire: Firestorm :fire: (2/2) - Two 2kk prizes for the first two people to level up 30 times', value=get_rank_info(firestorm2), inline=False)
    embed.add_field(name=':fire: Hellblaze :fire: - Two 2.5kk prizes for the first two people to level up 25 times', value=get_rank_info(hellblaze), inline=False)
    embed.add_field(name=':fire: Phoenix :fire: - Two 3kk prizes for the first two people to level up 20 times', value=get_rank_info(phoenix), inline=False)
    embed.add_field(name=':fire: Hellbringer :fire: - Two 3kk prizes for the first two people to level up 20 times', value=get_rank_info(hellbringer), inline=False)

    lens = [len(f.value) for f in embed.fields]
    if any([s > 900 for s in lens]):
        max_len = max(lens)
        embed.add_field(name='Warning', value=f'Almost reached the discord field size limit ({max_len}/1024)', inline=False)
    if any([s > 1023 for s in lens]):
        embed.clear_fields()
        embed.description = "Can't send message because it's too long, please tell Kikaro to fix his crappy code."

    return embed


def split_list(a_list):
    half = len(a_list)//2
    return a_list[:half], a_list[half:]


def get_rank_info(rank):
    if rank:
        rank_info = '\n'.join(rank)
        return rank_info
    else:
        return 'Nobody has gained any levels yet...'


def new_event():
    ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
    if not ids: next_id = 1
    else: next_id = max(ids) + 1
    open(pathlib.Path(script_path) / 'event' / f'{next_id}.dat', 'a').close()
    return discord.Embed(title='New event started', colour=0xffa32b, description=f'Started a new event with id {next_id}')


@tasks.loop(seconds=60)
async def new_joiner_loop():
    await bot.wait_until_ready()

    data = get_data()
    response = update_files(data)

    channel = bot.get_channel(new_joiner_channel)
    if response:
        await channel.send(embed=response)


def update_files(guild_chars):
    guild_file = pathlib.Path(script_path) / 'guild-members.dat'

    with open(guild_file) as f:
        original_guild_names = set(f.read().splitlines())
    guild_char_names = set(map(lambda x: x['name'], guild_chars))
    new_members = guild_char_names - original_guild_names

    if new_members:
        with guild_file.open('a') as f:
            f.write('\n'.join(list(new_members)) + '\n')

    messages = []
    for member in new_members:
        details = next(i for i in guild_chars if i['name'] == member)
        name = details['name']
        voc = details['vocation']
        lvl = details['level']
        messages.append(f'**{name}** ({lvl} {voc}) just joined the guild!')
    if messages:
        return discord.Embed(title='New members', colour=0xffa32b, description='\n'.join(messages))
    else: return None


def get_data():
    guild_url = 'https://api.tibiadata.com/v2/guild/Ashes+Remain.json'

    guild_json = requests.get(guild_url).json()

    try:
        guild_members = guild_json['guild']['members'][2:]
        guild_chars = list(itertools.chain(*nested_lookup('characters', guild_members)))
    except:
        guild_chars = []

    return guild_chars

@bot.event
async def on_ready():
    print(f'${bot.user} has connected')
    new_joiner_loop.start()


bot.run(TOKEN)
