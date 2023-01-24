#!/usr/bin/bash

# Usage: find src -iname "*.kt" -exec ./jda_rename_kotlin.sh {} \; -print

set -e

entities=net.dv8tion.jda.api.entities
files=$1

sed -i "s/import $entities\\.\\*/import $entities\\.\\*\nimport $entities.channel.*\nimport $entities.channel.attribute.*\nimport $entities.channel.middleman.*\nimport $entities.channel.concrete.*/" "$files"

sed -i "s/$entities.Channel/$entities.channel.Channel/" "$files"
sed -i "s/$entities.ChannelField/$entities.channel.ChannelField/" "$files"
sed -i "s/$entities.ChannelType/$entities.channel.ChannelType/" "$files"

sed -i "s/$entities.MessageChannel/$entities.channel.middleman.MessageChannel/" "$files"
sed -i "s/$entities.AudioChannel/$entities.channel.middleman.AudioChannel/" "$files"
sed -i "s/$entities.GuildChannel/$entities.channel.middleman.GuildChannel/" "$files"
sed -i "s/$entities.StandardGuildChannel/$entities.channel.middleman.StandardGuildChannel/" "$files"
sed -i "s/$entities.StandardGuildMessageChannel/$entities.channel.middleman.StandardGuildMessageChannel/" "$files"

sed -i "s/$entities.ICategorizableChannel/$entities.channel.attribute.ICategorizableChannel/" "$files"
sed -i "s/$entities.ICopyableChannel/$entities.channel.attribute.ICopyableChannel/" "$files"
sed -i "s/$entities.IGuildChannelContainer/$entities.channel.attribute.IGuildChannelContainer/" "$files"
sed -i "s/$entities.IInviteContainer/$entities.channel.attribute.IInviteContainer/" "$files"
sed -i "s/$entities.IMemberContainer/$entities.channel.attribute.IMemberContainer/" "$files"
sed -i "s/$entities.IPermissionContainer/$entities.channel.attribute.IPermissionContainer/" "$files"
sed -i "s/$entities.IPositionableContainer/$entities.channel.attribute.IPositionableContainer/" "$files"
sed -i "s/$entities.IThreadContainer/$entities.channel.attribute.IThreadContainer/" "$files"
sed -i "s/$entities.IWebhookContainer/$entities.channel.attribute.IWebhookContainer/" "$files"

sed -i "s/$entities.Category/$entities.channel.concrete.Category/" "$files"
sed -i "s/$entities.NewsChannel/$entities.channel.concrete.NewsChannel/" "$files"
sed -i "s/$entities.PrivateChannel/$entities.channel.concrete.PrivateChannel/" "$files"
sed -i "s/$entities.StageChannel/$entities.channel.concrete.StageChannel/" "$files"
sed -i "s/$entities.TextChannel/$entities.channel.concrete.TextChannel/" "$files"
sed -i "s/$entities.ThreadChannel/$entities.channel.concrete.ThreadChannel/" "$files"
sed -i "s/$entities.VoiceChannel/$entities.channel.concrete.VoiceChannel/" "$files"
