#!/usr/bin/env python
import asyncio
import os
import sys
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
activity_channel = int(os.getenv('ACTIVITY_CHANNEL'))

script_path = os.path.dirname(os.path.realpath(__file__))

bot = commands.Bot(command_prefix='!')


@bot.command(name='activity', help='List of active participants in the public-chat channel')
async def activity(ctx, *args):
    if 'Moderator' in [y.name for y in ctx.message.author.roles]:
        response = 'Test'
    else:
        response = 'You need the Moderator role to run this command'


    await ctx.send(response)


@bot.command(name='event', help='info: Provides an update on the level event.\nnew: Starts a new level event.')
async def event(ctx, *args):
    response = None

    arg1 = args[0]
    if arg1 == 'info':
        if len(args) > 1: event_id = args[1]
        else: event_id = None
        response = info(event_id)
    elif arg1 == 'rankups':
        if len(args) > 1: event_id = args[1]
        else: event_id = None
        response = rank_ups(event_id)
    elif arg1 == 'new':
        if 'Moderator' in [y.name for y in ctx.message.author.roles]:
            response = new_event()
        else:
            response = discord.Embed(title='Missing role', colour=0xffa32b, description='You need the Moderator role to run this command')

    if response:
        await ctx.send(embed=response)
    else:
        await ctx.send(f'Unknown argument **{arg1}** to event command')


def info(event_id=None):
    char_data = get_char_data(event_id)

    sorted_char_data = sorted(char_data, key=lambda x: x['end_level'] - x['start_level'])
    sorted_char_data.reverse()

    spark = []
    wildfire = []
    firestorm = []
    hellblaze = []
    hellbringer = []
    phoenix = []
    for char in sorted_char_data:
        name = char['name']
        start_level = char['start_level']
        end_level = char['end_level']
        gained = end_level - start_level
        if start_level < 200: rank = spark
        elif start_level < 400: rank = wildfire
        elif start_level < 600: rank = firestorm
        elif start_level < 800: rank = hellblaze
        elif start_level < 1000: rank = hellbringer
        else: rank = phoenix

        if gained != 0:
            emoji = '' if gained > 0 else ''
            # TODO mention if they ranked up
            level_s = 'level' if gained == 1 else 'levels'
            rank.append(f'**{name}**: {gained} {level_s} ({start_level} to {end_level}) {emoji}')

    embed = discord.Embed(title='Level Event Update', colour=0xffa32b)
    embed.add_field(name=':fire: Spark :fire: - Two 1kk prizes for the first two people to level up 35 times', value=get_rank_info(spark), inline=False)
    embed.add_field(name=':fire: Wildfire :fire: - Two 2kk prizes for the first two people to level up 35 times', value=get_rank_info(wildfire), inline=False)
    embed.add_field(name=':fire: Firestorm :fire: - Two 2kk prizes for the first two people to level up 30 times', value=get_rank_info(firestorm), inline=False)
    embed.add_field(name=':fire: Hellblaze :fire: - Two 2.5kk prizes for the first two people to level up 25 times', value=get_rank_info(hellblaze), inline=False)
    embed.add_field(name=':fire: Hellbringer :fire: - Two 3kk prizes for the first two people to level up 20 times', value=get_rank_info(hellbringer), inline=False)
    embed.add_field(name=':fire: Phoenix :fire: - Two 3kk prizes for the first two people to level up 15 times', value=get_rank_info(phoenix), inline=False)

    lens = [len(f.value) for f in embed.fields]
    print(lens)
    if any([s > 850 for s in lens]):
        max_len = max(lens)
        embed.add_field(name='Warning', value=f'Almost reached the discord field size limit ({max_len}/1024)', inline=False)
    if any([s > 1023 for s in lens]):
        embed.clear_fields()
        embed.description = "Can't send message because it's too long, please tell Kikaro to fix his crappy code."

    return embed


def get_char_data(event_id=None):
    if not event_id:
        ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
        event_id = max(ids)
    dat_file = pathlib.Path(script_path) / 'event' / f'{event_id}.dat.end'
    if not dat_file.exists():
        dat_file = pathlib.Path(script_path) / 'event' / f'{event_id}.dat'

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

    return char_data


def get_level_data(event_id=None):
    if not event_id:
        ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
        event_id = max(ids)
    dat_file = pathlib.Path(script_path) / 'event' / f'{event_id}.dat.end'
    if not dat_file.exists():
        dat_file = pathlib.Path(script_path) / 'event' / f'{event_id}.dat'

    with open(dat_file) as f:
        levels = f.read().splitlines()

    level_data = []

    for level in levels:
        d = datetime.strptime(level.split(',')[0], '%Y-%m-%d %H:%M:%S')
        name = level.split(',')[1]
        lvl = level.split(',')[2]
        level_data.append({'date': d, 'name': name, 'level': lvl})

    return level_data


def get_rank_info(rank):
    if rank:
        rank_info = '\n'.join(rank[:10])
        return rank_info
    else:
        return 'Nobody has gained any levels yet.'


def winners(event_id=None):
    level_data = get_level_data(event_id)

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


def rank_winners(level_data, start_level, end_level, levels_needed):
    pass


def rank_ups(event_id=None):
    char_data = get_char_data(event_id)

    sorted_char_data = sorted(char_data, key=lambda x: x['end_level'] - x['start_level'])
    sorted_char_data.reverse()

    rank_up_chars = []
    for char in sorted_char_data:
        name = char['name']
        start_level = char['start_level']
        end_level = char['end_level']
        rank_up_levels = [200, 300, 400, 600, 800, 1000]
        if start_level // 100 != end_level // 100 and \
            any(start_level < x and end_level >= x for x in rank_up_levels):
            rank_up_chars.append(f'**{name}**: {start_level} to {end_level}')

    if rank_up_chars:
        rank_up_message = '\n'.join(rank_up_chars)
    else:
        rank_up_message = 'Nobody has ranked up yet.'

    embed = discord.Embed(title='Level Event Rank Ups', colour=0xffa32b)
    embed.add_field(name='Rank ups:', value=rank_up_message, inline=False)
    return embed


def new_event():
    ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
    if not ids: next_id = 1
    else: next_id = max(ids) + 1
    open(pathlib.Path(script_path) / 'event' / f'{next_id}.dat', 'a').close()
    return discord.Embed(title='New event started', colour=0xffa32b, description=f'Started a new event with id {next_id}')


@tasks.loop(seconds=120)
async def new_joiner_loop():
    start_time = datetime.now().strftime('%H:%M:%S')
    await bot.wait_until_ready()
    ready_time = datetime.now().strftime('%H:%M:%S')

    # TODO this is not async, need to use grequests or something probably
    t1 = asyncio.create_task(get_data('Ashes+Remain'))
    t2 = asyncio.create_task(get_data('Ashes+Recharge'))
    remain_data = await t1
    recharge_data = await t2

    data_time = datetime.now().strftime('%H:%M:%S')

    response = update_files_and_get_new_joiner_message(remain_data, recharge_data)

    end_time = datetime.now().strftime('%H:%M:%S')
    print(f'{start_time} -> {ready_time} -> {data_time} -> {end_time}')
    channel = bot.get_channel(new_joiner_channel)
    if response:
        await channel.send(embed=response)


def update_files_and_get_new_joiner_message(remain_guild_chars, recharge_guild_chars):
    remain_guild_file = pathlib.Path(script_path) / 'remain-guild-members.dat'
    recharge_guild_file = pathlib.Path(script_path) / 'recharge-guild-members.dat'

    remain_new_members = get_new_members_and_update_file(remain_guild_file, remain_guild_chars)
    recharge_new_members = get_new_members_and_update_file(recharge_guild_file, recharge_guild_chars)

    messages = []
    for member in remain_new_members:
        messages.append(new_joiner_message(member, remain_guild_chars, 'Ashes Remain'))

    for member in recharge_new_members:
        messages.append(new_joiner_message(member, recharge_guild_chars, 'Ashes Recharge'))

    if messages:
        return discord.Embed(title='New members', colour=0xffa32b, description='\n'.join(messages))
    else: return None


def new_joiner_message(member, guild_chars, guild_name):
    details = next(i for i in guild_chars if i['name'] == member)
    name = details['name']
    voc = details['vocation']
    lvl = details['level']
    return f'**{name}** ({lvl} {voc}) just joined {guild_name}!'


def get_new_members_and_update_file(guild_file, guild_chars):
    with open(guild_file) as f:
        original_guild_names = set(f.read().splitlines())
    guild_char_names = set(map(lambda x: x['name'], guild_chars))

    new_members = guild_char_names - original_guild_names

    if new_members:
        with guild_file.open('a') as f:
            f.write('\n'.join(list(new_members)) + '\n')

    return new_members


async def get_data(guild_name):
    guild_url = f'https://api.tibiadata.com/v2/guild/{guild_name}.json'

    try:
        guild_json = requests.get(guild_url).json()
        guild_members = guild_json['guild']['members'][2:]
        guild_chars = list(itertools.chain(*nested_lookup('characters', guild_members)))
    except:
        guild_chars = []

    return guild_chars


@bot.event
async def on_ready():
    print(f'${bot.user} has connected')
    try:
        pass
        new_joiner_loop.start()
    except Exception as e:
        print(e)
        restart_program()


def restart_program():
    python = sys.executable

    sys.stdout.flush()

    args = [sys.argv[0]]
    print('Restart command recieved')
    os.execl(python, python, * args)


bot.run(TOKEN)
