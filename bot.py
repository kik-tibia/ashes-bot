#!/usr/bin/env python
import os
import discord
import pathlib
from discord.ext import commands
from dotenv import load_dotenv


load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')

bot = commands.Bot(command_prefix='!')


@bot.event
async def on_ready():
    print(f'${bot.user} has connected')


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
    script_path = os.path.dirname(os.path.realpath(__file__))
    # TODO customizable event file instead of hardcoded 1.dat
    dat_file = pathlib.Path(script_path) / 'event' / '1.dat'

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
            emoji = ':star:' if gained > 0 else ':FForRespects:'
            # TODO mention if they ranked up
            rank.append(f'**{name}** has advanced from **{start_level}** to **{end_level}**, a total of **{gained}** levels! {emoji}')

    # TODO find the first x people who advanced y levels in each rank
    embed = discord.Embed(title='Level Event Update (bot is still under development :construction_site:)', colour=0xffa32b)
    embed.add_field(name='Disclaimer', value='These are not the results of the April leveling event, this is a dummy event that started on Tuesday so that the bot can be tested in time for the actual May event!', inline=False)
    embed.add_field(name=':fire: Wildfire :fire:', value=get_rank_info(wildfire), inline=False)
    embed.add_field(name=':fire: Firestorm :fire:', value=get_rank_info(firestorm), inline=False)
    embed.add_field(name=':fire: Hellblaze :fire:', value=get_rank_info(hellblaze), inline=False)
    embed.add_field(name=':fire: Phoenix :fire:', value=get_rank_info(phoenix), inline=False)
    embed.add_field(name=':fire: Hellbringer :fire:', value=get_rank_info(hellbringer), inline=False)

    return embed


def get_rank_info(rank):
    if rank:
        return '\n'.join(rank)
    else:
        return 'Nobody has gained any levels yet...'


def new_event():
    script_path = os.path.dirname(os.path.realpath(__file__))
    ids = [int(x.stem) for x in list((pathlib.Path(script_path) / 'event').glob('*.dat'))]
    if not ids: next_id = 1
    else: next_id = max(ids) + 1
    open(pathlib.Path(script_path) / 'event' / f'{next_id}.dat', 'a').close()
    return discord.Embed(title='New event started', colour=0xffa32b, description=f'Started a new event with id {next_id}')


bot.run(TOKEN)
