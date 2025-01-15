import{_ as de}from"./MagicShare-DLx5ZzAx.js";import{d as H,c as s,r as Q,p as Y,a as ue,s as T,w as W,o as ce,b as ge,i as fe,_ as pe,e as me,f as b,u as he,g as ye,h as E,j as P,k as ve,l as xe}from"./index-BbaBpk3D.js";import{g as be,m as Se,_ as S,u as N,S as q,I as Ce,i as _e,c as K,R as Z,L as V,a as X,P as v,t as U,b as $e,d as we,M as ke}from"./LeftOutlined-D4xzlju3.js";const He=e=>!isNaN(parseFloat(e))&&isFinite(e),Be=e=>{const{componentCls:o,colorBgContainer:t,colorBgBody:l,colorText:a}=e;return{[`${o}-sider-light`]:{background:t,[`${o}-sider-trigger`]:{color:a,background:t},[`${o}-sider-zero-width-trigger`]:{color:a,background:t,border:`1px solid ${l}`,borderInlineStart:0}}}},Le=e=>{const{antCls:o,componentCls:t,colorText:l,colorTextLightSolid:a,colorBgHeader:g,colorBgBody:n,colorBgTrigger:d,layoutHeaderHeight:c,layoutHeaderPaddingInline:m,layoutHeaderColor:h,layoutFooterPadding:r,layoutTriggerHeight:u,layoutZeroTriggerSize:y,motionDurationMid:p,motionDurationSlow:i,fontSize:x,borderRadius:f}=e;return{[t]:S(S({display:"flex",flex:"auto",flexDirection:"column",color:l,minHeight:0,background:n,"&, *":{boxSizing:"border-box"},[`&${t}-has-sider`]:{flexDirection:"row",[`> ${t}, > ${t}-content`]:{width:0}},[`${t}-header, &${t}-footer`]:{flex:"0 0 auto"},[`${t}-header`]:{height:c,paddingInline:m,color:h,lineHeight:`${c}px`,background:g,[`${o}-menu`]:{lineHeight:"inherit"}},[`${t}-footer`]:{padding:r,color:l,fontSize:x,background:n},[`${t}-content`]:{flex:"auto",minHeight:0},[`${t}-sider`]:{position:"relative",minWidth:0,background:g,transition:`all ${p}, background 0s`,"&-children":{height:"100%",marginTop:-.1,paddingTop:.1,[`${o}-menu${o}-menu-inline-collapsed`]:{width:"auto"}},"&-has-trigger":{paddingBottom:u},"&-right":{order:1},"&-trigger":{position:"fixed",bottom:0,zIndex:1,height:u,color:a,lineHeight:`${u}px`,textAlign:"center",background:d,cursor:"pointer",transition:`all ${p}`},"&-zero-width":{"> *":{overflow:"hidden"},"&-trigger":{position:"absolute",top:c,insetInlineEnd:-y,zIndex:1,width:y,height:y,color:a,fontSize:e.fontSizeXL,display:"flex",alignItems:"center",justifyContent:"center",background:g,borderStartStartRadius:0,borderStartEndRadius:f,borderEndEndRadius:f,borderEndStartRadius:0,cursor:"pointer",transition:`background ${i} ease`,"&::after":{position:"absolute",inset:0,background:"transparent",transition:`all ${i}`,content:'""'},"&:hover::after":{background:"rgba(255, 255, 255, 0.2)"},"&-right":{insetInlineStart:-y,borderStartStartRadius:f,borderStartEndRadius:0,borderEndEndRadius:0,borderEndStartRadius:f}}}}},Be(e)),{"&-rtl":{direction:"rtl"}})}},Oe=be("Layout",e=>{const{colorText:o,controlHeightSM:t,controlHeight:l,controlHeightLG:a,marginXXS:g}=e,n=a*1.25,d=Se(e,{layoutHeaderHeight:l*2,layoutHeaderPaddingInline:n,layoutHeaderColor:o,layoutFooterPadding:`${t}px ${n}px`,layoutTriggerHeight:a+g*2,layoutZeroTriggerSize:a});return[Le(d)]},e=>{const{colorBgLayout:o}=e;return{colorBgHeader:"#001529",colorBgBody:o,colorBgTrigger:"#002140"}}),R=()=>({prefixCls:String,hasSider:{type:Boolean,default:void 0},tagName:String});function B(e){let{suffixCls:o,tagName:t,name:l}=e;return a=>H({compatConfig:{MODE:3},name:l,props:R(),setup(n,d){let{slots:c}=d;const{prefixCls:m}=N(o,n);return()=>{const h=S(S({},n),{prefixCls:m.value,tagName:t});return s(a,h,c)}}})}const A=H({compatConfig:{MODE:3},props:R(),setup(e,o){let{slots:t}=o;return()=>s(e.tagName,{class:e.prefixCls},t)}}),ze=H({compatConfig:{MODE:3},inheritAttrs:!1,props:R(),setup(e,o){let{slots:t,attrs:l}=o;const{prefixCls:a,direction:g}=N("",e),[n,d]=Oe(a),c=Q([]);Y(q,{addSider:r=>{c.value=[...c.value,r]},removeSider:r=>{c.value=c.value.filter(u=>u!==r)}});const h=ue(()=>{const{prefixCls:r,hasSider:u}=e;return{[d.value]:!0,[`${r}`]:!0,[`${r}-has-sider`]:typeof u=="boolean"?u:c.value.length>0,[`${r}-rtl`]:g.value==="rtl"}});return()=>{const{tagName:r}=e;return n(s(r,S(S({},l),{class:[h.value,l.class]}),t))}}}),M=B({suffixCls:"layout",tagName:"section",name:"ALayout"})(ze),$=B({suffixCls:"layout-header",tagName:"header",name:"ALayoutHeader"})(A),w=B({suffixCls:"layout-footer",tagName:"footer",name:"ALayoutFooter"})(A),k=B({suffixCls:"layout-content",tagName:"main",name:"ALayoutContent"})(A);var Te={icon:{tag:"svg",attrs:{viewBox:"0 0 1024 1024",focusable:"false"},children:[{tag:"path",attrs:{d:"M912 192H328c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h584c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm0 284H328c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h584c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zm0 284H328c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h584c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8zM104 228a56 56 0 10112 0 56 56 0 10-112 0zm0 284a56 56 0 10112 0 56 56 0 10-112 0zm0 284a56 56 0 10112 0 56 56 0 10-112 0z"}}]},name:"bars",theme:"outlined"};function G(e){for(var o=1;o<arguments.length;o++){var t=arguments[o]!=null?Object(arguments[o]):{},l=Object.keys(t);typeof Object.getOwnPropertySymbols=="function"&&(l=l.concat(Object.getOwnPropertySymbols(t).filter(function(a){return Object.getOwnPropertyDescriptor(t,a).enumerable}))),l.forEach(function(a){Ee(e,a,t[a])})}return e}function Ee(e,o,t){return o in e?Object.defineProperty(e,o,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[o]=t,e}var j=function(o,t){var l=G({},o,t.attrs);return s(Ce,G({},l,{icon:Te}),null)};j.displayName="BarsOutlined";j.inheritAttrs=!1;const J={xs:"479.98px",sm:"575.98px",md:"767.98px",lg:"991.98px",xl:"1199.98px",xxl:"1599.98px",xxxl:"1999.98px"},Pe=()=>({prefixCls:String,collapsible:{type:Boolean,default:void 0},collapsed:{type:Boolean,default:void 0},defaultCollapsed:{type:Boolean,default:void 0},reverseArrow:{type:Boolean,default:void 0},zeroWidthTriggerStyle:{type:Object,default:void 0},trigger:v.any,width:v.oneOfType([v.number,v.string]),collapsedWidth:v.oneOfType([v.number,v.string]),breakpoint:v.oneOf(U("xs","sm","md","lg","xl","xxl","xxxl")),theme:v.oneOf(U("light","dark")).def("dark"),onBreakpoint:Function,onCollapse:Function}),Me=(()=>{let e=0;return function(){let o=arguments.length>0&&arguments[0]!==void 0?arguments[0]:"";return e+=1,`${o}${e}`}})(),I=H({compatConfig:{MODE:3},name:"ALayoutSider",inheritAttrs:!1,props:_e(Pe(),{collapsible:!1,defaultCollapsed:!1,reverseArrow:!1,width:200,collapsedWidth:80}),emits:["breakpoint","update:collapsed","collapse"],setup(e,o){let{emit:t,attrs:l,slots:a}=o;const{prefixCls:g}=N("layout-sider",e),n=fe(q,void 0),d=T(!!(e.collapsed!==void 0?e.collapsed:e.defaultCollapsed)),c=T(!1);W(()=>e.collapsed,()=>{d.value=!!e.collapsed}),Y($e,d);const m=(i,x)=>{e.collapsed===void 0&&(d.value=i),t("update:collapsed",i),t("collapse",i,x)},h=T(i=>{c.value=i.matches,t("breakpoint",i.matches),d.value!==i.matches&&m(i.matches,"responsive")});let r;function u(i){return h.value(i)}const y=Me("ant-sider-");n&&n.addSider(y),ce(()=>{W(()=>e.breakpoint,()=>{try{r==null||r.removeEventListener("change",u)}catch{r==null||r.removeListener(u)}if(typeof window<"u"){const{matchMedia:i}=window;if(i&&e.breakpoint&&e.breakpoint in J){r=i(`(max-width: ${J[e.breakpoint]})`);try{r.addEventListener("change",u)}catch{r.addListener(u)}u(r)}}},{immediate:!0})}),ge(()=>{try{r==null||r.removeEventListener("change",u)}catch{r==null||r.removeListener(u)}n&&n.removeSider(y)});const p=()=>{m(!d.value,"clickTrigger")};return()=>{var i,x;const f=g.value,{collapsedWidth:D,width:ee,reverseArrow:L,zeroWidthTriggerStyle:te,trigger:_=(i=a.trigger)===null||i===void 0?void 0:i.call(a),collapsible:F,theme:oe}=e,O=d.value?D:ee,C=He(O)?`${O}px`:String(O),z=parseFloat(String(D||0))===0?s("span",{onClick:p,class:K(`${f}-zero-width-trigger`,`${f}-zero-width-trigger-${L?"right":"left"}`),style:te},[_||s(j,null,null)]):null,ne={expanded:L?s(Z,null,null):s(V,null,null),collapsed:L?s(V,null,null):s(Z,null,null)},re=d.value?"collapsed":"expanded",ae=ne[re],le=_!==null?z||s("div",{class:`${f}-trigger`,onClick:p,style:{width:C}},[_||ae]):null,ie=[l.style,{flex:`0 0 ${C}`,maxWidth:C,minWidth:C,width:C}],se=K(f,`${f}-${oe}`,{[`${f}-collapsed`]:!!d.value,[`${f}-has-trigger`]:F&&_!==null&&!z,[`${f}-below`]:!!c.value,[`${f}-zero-width`]:parseFloat(C)===0},l.class);return s("aside",X(X({},l),{},{class:se,style:ie}),[s("div",{class:`${f}-children`},[(x=a.default)===null||x===void 0?void 0:x.call(a)]),F||c.value&&z?le:null])}}}),Ie=$,Ne=w,Re=k,Ae=S(M,{Header:$,Footer:w,Content:k,Sider:I,install:e=>(e.component(M.name,M),e.component($.name,$),e.component(w.name,w),e.component(I.name,I),e.component(k.name,k),e)}),je={class:"layout-container"},De={__name:"LayoutContainer",setup(e){const o=he(),t=ve(),l=Q([o.path]),a=g=>{l.value!==g&&(t.push(g),l.value=[g])};return(g,n)=>{const d=we,c=ke,m=Ie,h=xe("router-view"),r=Re,u=Ne,y=Ae;return ye(),me("div",je,[s(y,{class:"layout"},{default:b(()=>[s(m,null,{default:b(()=>[n[7]||(n[7]=E("span",{class:"app-title"},"MagicShare",-1)),E("div",{class:"logo",onClick:n[0]||(n[0]=p=>a("/home"))},n[4]||(n[4]=[E("img",{class:"logo-img",src:de,height:"488",width:"522",alt:"MagicShareLogo"},null,-1)])),s(c,{selectedKeys:l.value,"onUpdate:selectedKeys":n[3]||(n[3]=p=>l.value=p),theme:"dark",mode:"horizontal",style:{lineHeight:"64px"}},{default:b(()=>[s(d,{key:"/home",onClick:n[1]||(n[1]=p=>a("/home"))},{default:b(()=>n[5]||(n[5]=[P("文件列表")])),_:1}),s(d,{key:"/about",onClick:n[2]||(n[2]=p=>a("/about"))},{default:b(()=>n[6]||(n[6]=[P("关于")])),_:1})]),_:1},8,["selectedKeys"])]),_:1}),s(r,null,{default:b(()=>[s(h)]),_:1}),s(u,{style:{"text-align":"center"}},{default:b(()=>n[8]||(n[8]=[P(" ZZHow ")])),_:1})]),_:1})])}}},Ze=pe(De,[["__scopeId","data-v-afaaeeed"]]);export{Ze as default};
