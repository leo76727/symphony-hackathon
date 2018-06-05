<messageML>
    <div class="entity" data-entity-id="todo">
        <card class="barStyle" iconSrc="https://cdn.rawgit.com/paulcreasey/symphony-hackathon/392456a9/src/main/resources/logo.png">
			<div>
			    <#if (entity['todo'].priority)=1>
                <img src="https://jira.atlassian.com/images/icons/priorities/highest.svg" class="icon"/>
                </#if>
				<#if (entity['todo'].priority)=2>
                <img src="https://jira.atlassian.com/images/icons/priorities/medium.svg" class="icon"/>
                </#if>
                <#if (entity['todo'].priority)=3>
                <img src="https://jira.atlassian.com/images/icons/priorities/low.svg" class="icon"/>
                </#if>
				<span class="tempo-text-color--link">
					TODO-${entity['todo'].id}
				</span>
				<span class="tempo-text-color--normal">${entity['todo'].summary}</span>

					<span class="tempo-text-color--link">@${entity['todo'].assigneeName}</span>
                    <#if (entity['todo'].assigneeName)="Jey Lin">
                    <img src="http://icons.iconarchive.com/icons/mattahan/ultrabuuf/32/Comics-Ironman-Hand-icon.png"/>
                    </#if>
                    <#if (entity['todo'].assigneeName)="Paul Creasey">
                    <img src="http://icons.iconarchive.com/icons/3xhumed/mega-games-pack-22/32/The-Incredible-Hulk-2-icon.png"/>
                    </#if>
                    <#if (entity['todo'].assigneeName)="David Lau">
                    <img src="http://icons.iconarchive.com/icons/diversity-avatars/avatars/32/batman-icon.png"/>
                    </#if>
                    <#if (entity['todo'].assigneeName)="Jimmy Tang">
                    <img src="http://icons.iconarchive.com/icons/mattahan/ultrabuuf/32/Comics-Captain-America-Shield-icon.png"/>
                    </#if>
                    <#if (entity['todo'].assigneeName)="Jiafeng Ni">
                    <img src="http://icons.iconarchive.com/icons/mattahan/ultrabuuf/32/Comics-Spiderman-Morales-icon.png"/>
                    </#if>
					<hr/>
                    <span class="tempo-text-color--green">${entity['action']}</span>
					<span class="tempo-text-color--secondary">&#160;&#160;&#160;Status:</span>
					<span class="tempo-bg-color--blue tempo-text-color--white tempo-token">
						${entity['todo'].status?upper_case}
					</span>
                    <#if (entity['todo'].due)??>
					    <span class="tempo-text-color--secondary">&#160;&#160;&#160;Due:</span>
                        <span class="tempo-text-color--normal">${entity['todo'].due}</span>
                    </#if>
					<#if (entity['todo'].labels)??>
						<span class="tempo-text-color--secondary">&#160;&#160;&#160;Labels:</span>
						<#list entity['todo'].labels as label>
							<span class="hashTag">#${label}</span>
						</#list>
					</#if>
			</div>
        </card>
    </div>
</messageML>
