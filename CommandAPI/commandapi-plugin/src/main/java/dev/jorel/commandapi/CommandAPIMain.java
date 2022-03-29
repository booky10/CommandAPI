/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi;

import java.io.File;
import java.util.Arrays;
import java.util.Map.Entry;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments._RegexArgument;

public class CommandAPIMain extends JavaPlugin {
	
	@Override
	public void onLoad() {		
		//Config loading
		saveDefaultConfig();
		CommandAPI.config = new Config(getConfig());
		CommandAPI.dispatcherFile = new File(getDataFolder(), "command_registration.json");
		CommandAPI.logger = getLogger();
		
		//Check dependencies for CommandAPI
		CommandAPIHandler.getInstance().checkDependencies();
		
		//Convert all plugins to be converted
		for(Entry<JavaPlugin, String[]> pluginToConvert : CommandAPI.config.getPluginsToConvert()) {
			if(pluginToConvert.getValue().length == 0) {
				Converter.convert(pluginToConvert.getKey());
			} else {
				for(String command : pluginToConvert.getValue()) {
					new AdvancedConverter(pluginToConvert.getKey(), command).convert();
				}
			}
		}
		
		// Convert all arbitrary commands		
		for(String commandName : CommandAPI.config.getCommandsToConvert()) {
			new AdvancedConverter(commandName).convertCommand();
		}

		inject();
	}
	
	@Override
	public void onEnable() {
		CommandAPI.onEnable(this);
		
		String simpleRegex = "(public|private) (static)? void main\\(String args\\[\\]\\) \\{\\}";
		new CommandAPICommand("hello")
			.withArguments(new _RegexArgument("val", simpleRegex).withRequirement(sender -> !(sender instanceof Player)))
			.withArguments(new IntegerArgument("intArg"))
			.executes((sender, args) -> {
				System.out.println(sender.getClass().getSimpleName());
				System.out.println(Arrays.deepToString(args));
				sender.sendMessage(String.valueOf(args[0]));
				sender.sendMessage(String.valueOf(args[1]));
			})
			.register();
	}
	
	public void inject() {
		CommandAPIHandler.getInstance().NMS.registerModdedArguments();
	}
}
