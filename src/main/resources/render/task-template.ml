<messageML>
    <div class="entity" data-entity-id="todo">
        <card class="barStyle" iconSrc="https://cdn.iconscout.com/public/images/icon/free/png-128/task-359bc93b6d7990cd-128x128.png">
			<div>
				<img src="https://jira.atlassian.com/images/icons/priorities/highest.svg" class="icon"/>
				<span class="tempo-text-color--link">
					TODO-${entity['todo'].id}
				</span>
				<span class="tempo-text-color--normal">${entity['todo'].summary}</span>

					<span class="tempo-text-color--link">@${entity['todo'].assigneeName}</span>

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
