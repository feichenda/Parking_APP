package com.lenovo.feizai.parking.adapter;

import android.content.Context;

import com.baidu.mapapi.search.sug.SuggestionResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author feizai
 * @date 02/17/2021 017 9:27:38 PM
 * @annotation
 */
public class AddressAdapter extends BaseQuickAdapter<SuggestionResult.SuggestionInfo, BaseViewHolder> {

    Context context;

    public AddressAdapter(Context context, @Nullable List<SuggestionResult.SuggestionInfo> data) {
        super(R.layout.address_item, data);
        this.context=context;
    }


    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, SuggestionResult.SuggestionInfo suggestionInfo) {
        baseViewHolder.setText(R.id.address_name,suggestionInfo.getKey());
        baseViewHolder.setText(R.id.address_info,suggestionInfo.getAddress());
    }
}
