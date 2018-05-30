<messageML>
    <div class="entity" data-entity-id="todos">
		<#list entity['todos'].items as todo>

				<div>
					<span class="tempo-text-color--link">
						TODO-${todo.id}
					</span>
					<span class="tempo-text-color--normal">${todo.summary}</span>

						<span class="tempo-text-color--link">@${todo.assigneeName}</span>


						<span class="tempo-text-color--secondary">&#160;&#160;&#160;Status:</span>
						<span class="tempo-bg-color--blue tempo-text-color--white tempo-token">
							${todo.status?upper_case}
						</span>
                        <#if (todo.due)??>
                            <span class="tempo-text-color--secondary">&#160;&#160;&#160;Due:</span>
                            <span class="tempo-text-color--normal">${todo.due}</span>
                        </#if>
						<#if (todo.labels)??>
							<span class="tempo-text-color--secondary">&#160;&#160;&#160;Labels:</span>
							<#list todo.labels as label>
								<span class="hashTag">#${label}</span>
							</#list>
						</#if>
				</div>

		</#list>
    </div>
</messageML>
